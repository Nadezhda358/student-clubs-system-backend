package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.event.EventListDto;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.event.Event;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.EventAudience;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.EventRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EventServiceEventListingOrderingTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    void getPublicEventsOrdersUpcomingBeforePastByDefault() {
        User creator = createTeacher();
        Club club = createActiveClub(creator);

        Event pastEvent = createPublishedEvent(club, creator, "Past", OffsetDateTime.now().minusDays(2));
        Event nearFutureEvent = createPublishedEvent(club, creator, "Near future", OffsetDateTime.now().plusDays(1));
        Event farFutureEvent = createPublishedEvent(club, creator, "Far future", OffsetDateTime.now().plusDays(3));

        Page<EventListDto> page = eventService.getPublicEvents(
                club.getId(),
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(page.getContent())
                .extracting(EventListDto::id)
                .containsExactly(nearFutureEvent.getId(), farFutureEvent.getId(), pastEvent.getId());
    }

    @Test
    void getPublicEventsKeepsExplicitClientSort() {
        User creator = createTeacher();
        Club club = createActiveClub(creator);

        Event pastEvent = createPublishedEvent(club, creator, "Past", OffsetDateTime.now().minusDays(2));
        Event nearFutureEvent = createPublishedEvent(club, creator, "Near future", OffsetDateTime.now().plusDays(1));
        Event farFutureEvent = createPublishedEvent(club, creator, "Far future", OffsetDateTime.now().plusDays(3));

        Page<EventListDto> page = eventService.getPublicEvents(
                club.getId(),
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "startAt"))
        );

        assertThat(page.getContent())
                .extracting(EventListDto::id)
                .containsExactly(pastEvent.getId(), nearFutureEvent.getId(), farFutureEvent.getId());
    }

    private Event createPublishedEvent(Club club, User creator, String titlePrefix, OffsetDateTime startAt) {
        Event event = new Event();
        event.setClub(club);
        event.setCreatedBy(creator);
        event.setTitle(titlePrefix + " " + UUID.randomUUID());
        event.setDescription("Event listing ordering test");
        event.setStartAt(startAt);
        event.setEndAt(startAt.plusHours(2));
        event.setLocation("Room 101");
        event.setStatus(EventStatus.PUBLISHED);
        event.setAudience(EventAudience.ALL_STUDENTS);

        return eventRepository.saveAndFlush(event);
    }

    private Club createActiveClub(User creator) {
        Club club = new Club();
        club.setName("Club " + UUID.randomUUID());
        club.setDescription("Event listing ordering club");
        club.setIsActive(true);
        club.setCreatedBy(creator);
        return clubRepository.saveAndFlush(club);
    }

    private User createTeacher() {
        String suffix = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("teacher-" + suffix + "@example.com");
        user.setPasswordHash("password-hash");
        user.setFirstName("Teacher");
        user.setLastName("Tester");
        user.setRole(UserRole.TEACHER);

        return userRepository.saveAndFlush(user);
    }
}
