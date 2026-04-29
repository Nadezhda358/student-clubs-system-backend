package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.event.EventParticipationDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.EventListDto;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.event.Event;
import com.school.ppmg.student_clubs_system_api.entities.event.EventRegistration;
import com.school.ppmg.student_clubs_system_api.entities.event.EventRegistrationId;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.EventAudience;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import com.school.ppmg.student_clubs_system_api.enums.RegistrationStatus;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.EventRegistrationRepository;
import com.school.ppmg.student_clubs_system_api.repositories.EventRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EventServiceSoftDeleteSafetyTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void adminEventsSkipEventsWhoseClubIsDeleted() {
        User admin = createUser(UserRole.ADMIN);
        Club availableClub = createActiveClub(admin);
        Club deletedClub = createActiveClub(admin);
        Event availableEvent = createEvent(availableClub, admin, "available-event");
        Event hiddenEvent = createEvent(deletedClub, admin, "hidden-event");
        softDeleteClub(deletedClub.getId());
        entityManager.flush();
        entityManager.clear();

        authenticate(admin);
        try {
            Page<EventListDto> page = eventService.getAdminEvents(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    PageRequest.of(0, 10)
            );

            assertThat(page.getContent()).extracting(EventListDto::id)
                    .contains(availableEvent.getId())
                    .doesNotContain(hiddenEvent.getId());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void adminParticipationsSkipRegistrationsWhoseClubIsDeleted() {
        User admin = createUser(UserRole.ADMIN);
        User student = createUser(UserRole.STUDENT);
        Club availableClub = createActiveClub(admin);
        Club deletedClub = createActiveClub(admin);
        Event availableEvent = createEvent(availableClub, admin, "available-event");
        Event hiddenEvent = createEvent(deletedClub, admin, "hidden-event");
        createRegistration(availableEvent, student);
        createRegistration(hiddenEvent, student);
        softDeleteClub(deletedClub.getId());
        entityManager.flush();
        entityManager.clear();

        authenticate(admin);
        try {
            Page<EventParticipationDto> page = eventService.getAdminParticipations(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    PageRequest.of(0, 10)
            );

            assertThat(page.getContent()).extracting(EventParticipationDto::eventId)
                    .contains(availableEvent.getId())
                    .doesNotContain(hiddenEvent.getId());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void deleteAdminEventSoftDeletesItsRegistrations() {
        User admin = createUser(UserRole.ADMIN);
        User student = createUser(UserRole.STUDENT);
        Club club = createActiveClub(admin);
        Event event = createEvent(club, admin, "deleted-event");
        EventRegistration registration = createRegistration(event, student);
        EventRegistrationId registrationId = registration.getId();

        authenticate(admin);
        try {
            eventService.deleteAdminEvent(event.getId());
            entityManager.flush();
            entityManager.clear();

            assertThat(eventRegistrationRepository.findById(registrationId)).isEmpty();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void softDeleteClub(Long clubId) {
        entityManager.createNativeQuery("""
                        update clubs
                        set deleted_at = CURRENT_TIMESTAMP(6)
                        where id = :clubId
                        """)
                .setParameter("clubId", clubId)
                .executeUpdate();
    }

    private EventRegistration createRegistration(Event event, User student) {
        EventRegistration registration = new EventRegistration();
        registration.setId(new EventRegistrationId(event.getId(), student.getId()));
        registration.setEvent(event);
        registration.setStudent(student);
        registration.setStatus(RegistrationStatus.REGISTERED);
        registration.setRegisteredAt(OffsetDateTime.now());
        return eventRegistrationRepository.saveAndFlush(registration);
    }

    private Event createEvent(Club club, User creator, String titlePrefix) {
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(2);
        Event event = new Event();
        event.setClub(club);
        event.setCreatedBy(creator);
        event.setTitle(titlePrefix + " " + UUID.randomUUID());
        event.setDescription("Event soft-delete safety test");
        event.setStartAt(startAt);
        event.setEndAt(startAt.plusHours(2));
        event.setLocation("Room 101");
        event.setStatus(EventStatus.PUBLISHED);
        event.setAudience(EventAudience.ALL_STUDENTS);
        return eventRepository.saveAndFlush(event);
    }

    private Club createActiveClub(User creator) {
        Club club = new Club();
        club.setName("Event safety club " + UUID.randomUUID());
        club.setDescription("Event soft-delete safety club");
        club.setIsActive(true);
        club.setCreatedBy(creator);
        return clubRepository.saveAndFlush(club);
    }

    private User createUser(UserRole role) {
        String suffix = UUID.randomUUID().toString();
        User user = new User();
        user.setEmail(role.name().toLowerCase() + "-" + suffix + "@example.com");
        user.setPasswordHash("password-hash");
        user.setFirstName(role.name().substring(0, 1) + role.name().substring(1).toLowerCase());
        user.setLastName("Tester");
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of())
        );
    }
}
