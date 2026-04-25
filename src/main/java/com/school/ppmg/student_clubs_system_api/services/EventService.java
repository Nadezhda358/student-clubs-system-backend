package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.event.EventDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.EventListDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.EventParticipationDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.MyEventDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.UpsertEventDto;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.event.Event;
import com.school.ppmg.student_clubs_system_api.entities.event.EventRegistration;
import com.school.ppmg.student_clubs_system_api.entities.event.EventRegistrationId;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.EventAudience;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import com.school.ppmg.student_clubs_system_api.enums.EventTimeFilter;
import com.school.ppmg.student_clubs_system_api.enums.MembershipStatus;
import com.school.ppmg.student_clubs_system_api.enums.RegistrationStatus;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.exceptions.ConflictException;
import com.school.ppmg.student_clubs_system_api.exceptions.ResourceNotFoundException;
import com.school.ppmg.student_clubs_system_api.repositories.ClubMembershipRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubTeacherRepository;
import com.school.ppmg.student_clubs_system_api.repositories.EventRegistrationRepository;
import com.school.ppmg.student_clubs_system_api.repositories.EventRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final ClubRepository clubRepository;
    private final ClubTeacherRepository clubTeacherRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final AuthService authService;
    private final S3StorageService s3StorageService;

    @Transactional(readOnly = true)
    public Page<EventListDto> getPublicEvents(
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            EventTimeFilter timeFilter,
            Pageable pageable
    ) {
        EventWriteValidator.validateSearchRange(from, to);

        Page<Event> page = findEventListingPage(
                publicEventsSpecification(clubId, normalizeQuery(q), from, to, defaultAll(timeFilter)),
                pageable
        );

        return page.map(this::toListDto);
    }

    @Transactional(readOnly = true)
    public EventDto getPublicById(Long id) {
        Event event = eventRepository.findOne(publicEventByIdSpecification(id))
                .orElseThrow(() -> new ResourceNotFoundException("Event with id=" + id + " not found"));

        return toDto(event);
    }

    @Transactional(readOnly = true)
    public Page<MyEventDto> getMyEvents(
            RegistrationStatus registrationStatus,
            EventStatus eventStatus,
            String q,
            EventTimeFilter timeFilter,
            Pageable pageable
    ) {
        User student = getCurrentStudent();

        Page<EventRegistration> page = eventRegistrationRepository.findAll(
                myEventsSpecification(
                        student.getId(),
                        registrationStatus,
                        eventStatus,
                        normalizeQuery(q),
                        defaultUpcoming(timeFilter)
                ),
                withFixedSort(pageable, Sort.by(Sort.Direction.ASC, "event_startAt"))
        );

        return page.map(this::toMyEventDto);
    }

    @Transactional
    public EventParticipationDto registerCurrentStudent(Long eventId) {
        User student = getCurrentStudent();
        Event event = getEventOrThrow(eventId);

        ensureStudentCanRegister(student, event);

        EventRegistrationId registrationId = new EventRegistrationId(event.getId(), student.getId());
        EventRegistration registration = eventRegistrationRepository.findById(registrationId).orElse(null);
        OffsetDateTime now = OffsetDateTime.now();

        if (registration != null) {
            if (registration.getStatus() == RegistrationStatus.REGISTERED) {
                throw new ConflictException("You are already registered for this event");
            }

            if (registration.getStatus() != RegistrationStatus.CANCELLED) {
                throw new ConflictException("This participation cannot be registered again");
            }

            ensureCapacityAvailable(event);
            registration.setStatus(RegistrationStatus.REGISTERED);
            registration.setRegisteredAt(now);
            registration.setCancelledAt(null);
            return toParticipationDto(eventRegistrationRepository.save(registration));
        }

        ensureCapacityAvailable(event);

        EventRegistration newRegistration = new EventRegistration();
        newRegistration.setId(registrationId);
        newRegistration.setEvent(event);
        newRegistration.setStudent(student);
        newRegistration.setStatus(RegistrationStatus.REGISTERED);
        newRegistration.setRegisteredAt(now);
        newRegistration.setCancelledAt(null);

        return toParticipationDto(eventRegistrationRepository.save(newRegistration));
    }

    @Transactional
    public EventParticipationDto cancelCurrentStudentRegistration(Long eventId) {
        User student = getCurrentStudent();
        EventRegistration registration = getRegistrationOrThrow(eventId, student.getId());

        if (registration.getStatus() != RegistrationStatus.REGISTERED) {
            throw new ConflictException("Only active registrations can be cancelled");
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime deadline = getEffectiveRegistrationDeadline(registration.getEvent());
        if (now.isAfter(deadline)) {
            throw new ConflictException("Registration can no longer be cancelled after the registration deadline");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancelledAt(now);

        return toParticipationDto(eventRegistrationRepository.save(registration));
    }

    @Transactional(readOnly = true)
    public Page<EventListDto> getTeacherEvents(
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            EventTimeFilter timeFilter,
            EventStatus status,
            Pageable pageable
    ) {
        User teacher = getCurrentTeacher();
        ensureTeacherCanManageFilteredClub(teacher, clubId);
        EventWriteValidator.validateSearchRange(from, to);

        Page<Event> page = findEventListingPage(
                teacherEventsSpecification(
                        teacher.getId(),
                        clubId,
                        normalizeQuery(q),
                        from,
                        to,
                        defaultAll(timeFilter),
                        status
                ),
                pageable
        );

        return page.map(this::toListDto);
    }

    @Transactional(readOnly = true)
    public EventDto getTeacherEventById(Long id) {
        return toDto(getTeacherManagedEventOrThrow(id));
    }

    @Transactional
    public EventDto createTeacherEvent(UpsertEventDto dto) {
        User teacher = getCurrentTeacher();
        Club club = getClubOrThrow(dto.clubId());
        ensureTeacherCanManageClub(teacher, club.getId());
        EventWriteValidator.validateForCreate(dto, OffsetDateTime.now());

        Event event = new Event();
        event.setCreatedBy(teacher);
        applyUpsert(event, dto, club);

        return toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto updateTeacherEvent(Long id, UpsertEventDto dto) {
        User teacher = getCurrentTeacher();
        Event event = getTeacherManagedEventOrThrow(id);
        Club club = getClubOrThrow(dto.clubId());
        ensureTeacherCanManageClub(teacher, club.getId());
        EventWriteValidator.validateForUpdate(event, dto, OffsetDateTime.now());

        applyUpsert(event, dto, club);
        return toDto(eventRepository.save(event));
    }

    @Transactional
    public void deleteTeacherEvent(Long id) {
        eventRepository.delete(getTeacherManagedEventOrThrow(id));
    }

    @Transactional
    public EventDto updateTeacherMainImage(Long id, MultipartFile file) {
        return updateEventMainImage(getTeacherManagedEventOrThrow(id), file);
    }

    @Transactional(readOnly = true)
    public Page<EventParticipationDto> getTeacherParticipants(
            Long eventId,
            RegistrationStatus status,
            String q,
            Pageable pageable
    ) {
        User teacher = getCurrentTeacher();
        Event event = getEventOrThrow(eventId);
        ensureTeacherCanManageClub(teacher, event.getClub().getId());

        return getParticipantsForEvent(eventId, status, q, pageable);
    }

    @Transactional
    public EventParticipationDto updateTeacherParticipationStatus(
            Long eventId,
            Long studentId,
            RegistrationStatus newStatus
    ) {
        User teacher = getCurrentTeacher();
        Event event = getEventOrThrow(eventId);
        ensureTeacherCanManageClub(teacher, event.getClub().getId());

        return updateParticipationStatus(event, studentId, newStatus);
    }

    @Transactional(readOnly = true)
    public Page<EventListDto> getAdminEvents(
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            EventTimeFilter timeFilter,
            EventStatus status,
            Pageable pageable
    ) {
        requireAdmin();
        EventWriteValidator.validateSearchRange(from, to);

        Page<Event> page = findEventListingPage(
                adminEventsSpecification(clubId, normalizeQuery(q), from, to, defaultAll(timeFilter), status),
                pageable
        );

        return page.map(this::toListDto);
    }

    @Transactional(readOnly = true)
    public EventDto getAdminEventById(Long id) {
        requireAdmin();
        return toDto(getEventOrThrow(id));
    }

    @Transactional
    public EventDto createAdminEvent(UpsertEventDto dto) {
        User admin = requireAdmin();
        Club club = getClubOrThrow(dto.clubId());
        EventWriteValidator.validateForCreate(dto, OffsetDateTime.now());

        Event event = new Event();
        event.setCreatedBy(admin);
        applyUpsert(event, dto, club);

        return toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto updateAdminEvent(Long id, UpsertEventDto dto) {
        requireAdmin();
        Event event = getEventOrThrow(id);
        Club club = getClubOrThrow(dto.clubId());
        EventWriteValidator.validateForUpdate(event, dto, OffsetDateTime.now());

        applyUpsert(event, dto, club);
        return toDto(eventRepository.save(event));
    }

    @Transactional
    public void deleteAdminEvent(Long id) {
        requireAdmin();
        eventRepository.delete(getEventOrThrow(id));
    }

    @Transactional
    public EventDto updateAdminMainImage(Long id, MultipartFile file) {
        requireAdmin();
        return updateEventMainImage(getEventOrThrow(id), file);
    }

    @Transactional(readOnly = true)
    public Page<EventParticipationDto> getAdminParticipations(
            Long clubId,
            Long eventId,
            RegistrationStatus registrationStatus,
            EventStatus eventStatus,
            String q,
            EventTimeFilter timeFilter,
            Pageable pageable
    ) {
        requireAdmin();

        Page<EventRegistration> page = eventRegistrationRepository.findAll(
                adminParticipationsSpecification(
                        clubId,
                        eventId,
                        registrationStatus,
                        eventStatus,
                        normalizeQuery(q),
                        defaultAll(timeFilter)
                ),
                withFixedSort(pageable, Sort.by(Sort.Direction.ASC, "event_startAt"))
        );

        return page.map(this::toParticipationDto);
    }

    @Transactional(readOnly = true)
    public Page<EventParticipationDto> getAdminParticipantsForEvent(
            Long eventId,
            RegistrationStatus status,
            String q,
            Pageable pageable
    ) {
        requireAdmin();
        getEventOrThrow(eventId);
        return getParticipantsForEvent(eventId, status, q, pageable);
    }

    @Transactional
    public EventParticipationDto updateAdminParticipationStatus(
            Long eventId,
            Long studentId,
            RegistrationStatus newStatus
    ) {
        requireAdmin();
        Event event = getEventOrThrow(eventId);
        return updateParticipationStatus(event, studentId, newStatus);
    }

    private Page<EventParticipationDto> getParticipantsForEvent(
            Long eventId,
            RegistrationStatus status,
            String q,
            Pageable pageable
    ) {
        Page<EventRegistration> page = eventRegistrationRepository.findAll(
                eventParticipantsSpecification(eventId, status, normalizeQuery(q)),
                withFixedSort(pageable, Sort.by(Sort.Direction.ASC, "registeredAt"))
        );

        return page.map(this::toParticipationDto);
    }

    private EventParticipationDto updateParticipationStatus(
            Event event,
            Long studentId,
            RegistrationStatus newStatus
    ) {
        validateManagedParticipationStatus(newStatus);

        EventRegistration registration = getRegistrationOrThrow(event.getId(), studentId);
        OffsetDateTime now = OffsetDateTime.now();

        if (newStatus == registration.getStatus()) {
            return toParticipationDto(registration);
        }

        if (newStatus == RegistrationStatus.REGISTERED) {
            if (registration.getStatus() != RegistrationStatus.CANCELLED) {
                throw new ConflictException("Only cancelled participations can be reopened");
            }

            ensureManagedRegistrationAllowed(event, registration.getStudent());
            registration.setStatus(RegistrationStatus.REGISTERED);
            registration.setRegisteredAt(now);
            registration.setCancelledAt(null);
        } else {
            if (registration.getStatus() != RegistrationStatus.REGISTERED) {
                throw new ConflictException("Only active registrations can be cancelled");
            }

            registration.setStatus(RegistrationStatus.CANCELLED);
            registration.setCancelledAt(now);
        }

        return toParticipationDto(eventRegistrationRepository.save(registration));
    }

    private Page<Event> findEventListingPage(Specification<Event> specification, Pageable pageable) {
        Pageable effectivePageable = pageable == null ? Pageable.unpaged() : pageable;
        Specification<Event> effectiveSpecification = shouldApplyDefaultEventListingOrder(effectivePageable)
                ? withUpcomingEventsFirstOrder(specification)
                : specification;

        return eventRepository.findAll(effectiveSpecification, effectivePageable);
    }

    private void validateManagedParticipationStatus(RegistrationStatus status) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        if (status != RegistrationStatus.REGISTERED && status != RegistrationStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only REGISTERED and CANCELLED statuses are supported"
            );
        }
    }

    private void ensureStudentCanRegister(User student, Event event) {
        if (!Boolean.TRUE.equals(event.getClub().getIsActive())) {
            throw new ConflictException("The club for this event is inactive");
        }

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ConflictException("This event is not open for student registration");
        }

        if (OffsetDateTime.now().isAfter(getEffectiveRegistrationDeadline(event))) {
            throw new ConflictException("Registration deadline has passed for this event");
        }

        ensureAudienceEligibility(event, student);
    }

    private void ensureManagedRegistrationAllowed(Event event, User student) {
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ConflictException("Cannot register participants for a cancelled event");
        }

        ensureAudienceEligibility(event, student);
        ensureCapacityAvailable(event);
    }

    private void ensureAudienceEligibility(Event event, User student) {
        if (event.getAudience() == null || event.getAudience() == EventAudience.ALL_STUDENTS) {
            return;
        }

        boolean isMember = clubMembershipRepository.existsByClub_IdAndStudent_IdAndStatus(
                event.getClub().getId(),
                student.getId(),
                MembershipStatus.ACTIVE
        );

        if (!isMember) {
            throw new ConflictException("This event is available only to approved members of the club");
        }
    }

    private void ensureCapacityAvailable(Event event) {
        Integer capacity = event.getCapacity();
        if (capacity == null) {
            return;
        }

        long registeredCount = getRegisteredCount(event);
        if (registeredCount >= capacity) {
            throw new ConflictException("Event capacity has been reached");
        }
    }

    private EventRegistration getRegistrationOrThrow(Long eventId, Long studentId) {
        EventRegistrationId id = new EventRegistrationId(eventId, studentId);
        return eventRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Participation for event id=" + eventId + " and student id=" + studentId + " not found"
                ));
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event with id=" + id + " not found"));
    }

    private Club getClubOrThrow(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club with id=" + clubId + " not found"));
    }

    private Event getTeacherManagedEventOrThrow(Long id) {
        User teacher = getCurrentTeacher();
        Event event = getEventOrThrow(id);
        ensureTeacherCanManageClub(teacher, event.getClub().getId());
        return event;
    }

    private void applyUpsert(Event event, UpsertEventDto dto, Club club) {
        event.setClub(club);
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setStartAt(dto.startAt());
        event.setEndAt(dto.endAt());
        event.setLocation(dto.location());
        event.setCapacity(dto.capacity());
        event.setRegistrationDeadline(dto.registrationDeadline());
        event.setStatus(dto.status());
        event.setAudience(dto.audience());
    }

    private Specification<Event> publicEventsSpecification(
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            EventTimeFilter timeFilter
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> club = root.join("club");
            List<Predicate> predicates = buildCommonEventPredicates(root, club, cb, clubId, q, from, to, timeFilter, null);
            predicates.add(cb.equal(root.get("status"), EventStatus.PUBLISHED));
            predicates.add(cb.isTrue(club.get("isActive")));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Event> publicEventByIdSpecification(Long id) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("id"), id),
                cb.equal(root.get("status"), EventStatus.PUBLISHED),
                cb.isTrue(root.join("club").get("isActive"))
        );
    }

    private Specification<Event> teacherEventsSpecification(
            Long teacherId,
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            EventTimeFilter timeFilter,
            EventStatus status
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> club = root.join("club");
            Join<Object, Object> teachers = club.join("teachers");
            query.distinct(true);

            List<Predicate> predicates = buildCommonEventPredicates(root, club, cb, clubId, q, from, to, timeFilter, status);
            predicates.add(cb.equal(teachers.get("teacher").get("id"), teacherId));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Event> adminEventsSpecification(
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            EventTimeFilter timeFilter,
            EventStatus status
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> club = root.join("club");
            List<Predicate> predicates = buildCommonEventPredicates(root, club, cb, clubId, q, from, to, timeFilter, status);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private List<Predicate> buildCommonEventPredicates(
            jakarta.persistence.criteria.Root<Event> root,
            Join<Object, Object> club,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            EventTimeFilter timeFilter,
            EventStatus status
    ) {
        List<Predicate> predicates = new ArrayList<>();

        if (clubId != null) {
            predicates.add(cb.equal(club.get("id"), clubId));
        }

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        applyTimeFilter(predicates, cb, root.get("startAt"), timeFilter);

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("startAt"), from));
        }

        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("startAt"), to));
        }

        if (q != null) {
            String like = "%" + q.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("location"), "")), like),
                    cb.like(cb.lower(club.get("name")), like)
            ));
        }

        return predicates;
    }

    private Specification<EventRegistration> myEventsSpecification(
            Long studentId,
            RegistrationStatus registrationStatus,
            EventStatus eventStatus,
            String q,
            EventTimeFilter timeFilter
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> event = root.join("event");
            Join<Object, Object> club = event.join("club");
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("student").get("id"), studentId));

            if (registrationStatus != null) {
                predicates.add(cb.equal(root.get("status"), registrationStatus));
            }

            if (eventStatus != null) {
                predicates.add(cb.equal(event.get("status"), eventStatus));
            }

            applyTimeFilter(predicates, cb, event.get("startAt"), timeFilter);

            if (q != null) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(event.get("title")), like),
                        cb.like(cb.lower(event.get("description")), like),
                        cb.like(cb.lower(cb.coalesce(event.get("location"), "")), like),
                        cb.like(cb.lower(club.get("name")), like)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<EventRegistration> eventParticipantsSpecification(
            Long eventId,
            RegistrationStatus status,
            String q
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> student = root.join("student");
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("event").get("id"), eventId));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (q != null) {
                String like = "%" + q.toLowerCase() + "%";
                Expression<String> fullName = cb.lower(cb.concat(cb.concat(student.get("firstName"), " "), student.get("lastName")));
                predicates.add(cb.or(
                        cb.like(cb.lower(student.get("email")), like),
                        cb.like(fullName, like)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<EventRegistration> adminParticipationsSpecification(
            Long clubId,
            Long eventId,
            RegistrationStatus registrationStatus,
            EventStatus eventStatus,
            String q,
            EventTimeFilter timeFilter
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> event = root.join("event");
            Join<Object, Object> club = event.join("club");
            Join<Object, Object> student = root.join("student");
            List<Predicate> predicates = new ArrayList<>();

            if (clubId != null) {
                predicates.add(cb.equal(club.get("id"), clubId));
            }

            if (eventId != null) {
                predicates.add(cb.equal(event.get("id"), eventId));
            }

            if (registrationStatus != null) {
                predicates.add(cb.equal(root.get("status"), registrationStatus));
            }

            if (eventStatus != null) {
                predicates.add(cb.equal(event.get("status"), eventStatus));
            }

            applyTimeFilter(predicates, cb, event.get("startAt"), timeFilter);

            if (q != null) {
                String like = "%" + q.toLowerCase() + "%";
                Expression<String> fullName = cb.lower(cb.concat(cb.concat(student.get("firstName"), " "), student.get("lastName")));
                predicates.add(cb.or(
                        cb.like(cb.lower(event.get("title")), like),
                        cb.like(cb.lower(club.get("name")), like),
                        cb.like(cb.lower(student.get("email")), like),
                        cb.like(fullName, like)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void applyTimeFilter(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            Path<OffsetDateTime> startAt,
            EventTimeFilter timeFilter
    ) {
        EventTimeFilter effectiveFilter = timeFilter == null ? EventTimeFilter.ALL : timeFilter;
        OffsetDateTime now = OffsetDateTime.now();

        if (effectiveFilter == EventTimeFilter.UPCOMING) {
            predicates.add(cb.greaterThanOrEqualTo(startAt, now));
        } else if (effectiveFilter == EventTimeFilter.PAST) {
            predicates.add(cb.lessThan(startAt, now));
        }
    }

    private EventListDto toListDto(Event event) {
        long registeredCount = getRegisteredCount(event);
        return new EventListDto(
                event.getId(),
                event.getClub().getId(),
                event.getClub().getName(),
                event.getTitle(),
                event.getDescription(),
                event.getStartAt(),
                event.getEndAt(),
                event.getLocation(),
                resolveImageUrl(event.getMainImageUrl()),
                event.getCapacity(),
                registeredCount,
                getAvailableSpots(event, registeredCount),
                event.getRegistrationDeadline(),
                getEffectiveRegistrationDeadline(event),
                isRegistrationOpen(event),
                event.getStatus(),
                event.getAudience()
        );
    }

    private EventDto toDto(Event event) {
        long registeredCount = getRegisteredCount(event);
        User createdBy = event.getCreatedBy();

        return new EventDto(
                event.getId(),
                event.getClub().getId(),
                event.getClub().getName(),
                event.getTitle(),
                event.getDescription(),
                event.getStartAt(),
                event.getEndAt(),
                event.getLocation(),
                resolveImageUrl(event.getMainImageUrl()),
                event.getCapacity(),
                registeredCount,
                getAvailableSpots(event, registeredCount),
                event.getRegistrationDeadline(),
                getEffectiveRegistrationDeadline(event),
                isRegistrationOpen(event),
                event.getStatus(),
                event.getAudience(),
                createdBy != null ? createdBy.getId() : null,
                createdBy != null ? createdBy.getFirstName() + " " + createdBy.getLastName() : null,
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    private MyEventDto toMyEventDto(EventRegistration registration) {
        Event event = registration.getEvent();
        return new MyEventDto(
                event.getId(),
                event.getClub().getId(),
                event.getClub().getName(),
                event.getTitle(),
                event.getStartAt(),
                event.getEndAt(),
                event.getLocation(),
                resolveImageUrl(event.getMainImageUrl()),
                event.getStatus(),
                event.getAudience(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getCancelledAt(),
                event.getRegistrationDeadline(),
                getEffectiveRegistrationDeadline(event)
        );
    }

    private EventParticipationDto toParticipationDto(EventRegistration registration) {
        Event event = registration.getEvent();
        User student = registration.getStudent();

        return new EventParticipationDto(
                event.getId(),
                event.getTitle(),
                event.getClub().getId(),
                event.getClub().getName(),
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                student.getEmail(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getCancelledAt(),
                event.getStatus(),
                event.getStartAt(),
                event.getEndAt()
        );
    }

    private long getRegisteredCount(Event event) {
        return eventRegistrationRepository.countByEvent_IdAndStatus(event.getId(), RegistrationStatus.REGISTERED);
    }

    private EventDto updateEventMainImage(Event event, MultipartFile file) {
        String url = s3StorageService.upload(file, "events/" + event.getId() + "/main-image");
        event.setMainImageUrl(url);
        return toDto(eventRepository.save(event));
    }

    private String resolveImageUrl(String value) {
        return s3StorageService.resolveReadUrl(value);
    }

    private Long getAvailableSpots(Event event, long registeredCount) {
        if (event.getCapacity() == null) {
            return null;
        }

        return Math.max(event.getCapacity() - registeredCount, 0);
    }

    private OffsetDateTime getEffectiveRegistrationDeadline(Event event) {
        return event.getRegistrationDeadline() != null ? event.getRegistrationDeadline() : event.getStartAt();
    }

    private boolean isRegistrationOpen(Event event) {
        return event.getStatus() == EventStatus.PUBLISHED && !OffsetDateTime.now().isAfter(getEffectiveRegistrationDeadline(event));
    }

    private User getCurrentStudent() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        }
        return currentUser;
    }

    private User getCurrentTeacher() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != UserRole.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher access required");
        }
        return currentUser;
    }

    private User requireAdmin() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return currentUser;
    }

    private void ensureTeacherCanManageClub(User teacher, Long clubId) {
        if (!clubTeacherRepository.existsByClub_IdAndTeacher_Id(clubId, teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not manage this club");
        }
    }

    private void ensureTeacherCanManageFilteredClub(User teacher, Long clubId) {
        if (clubId != null) {
            ensureTeacherCanManageClub(teacher, clubId);
        }
    }

    private Pageable withFixedSort(Pageable pageable, Sort sort) {
        if (pageable == null || pageable.isUnpaged()) {
            return pageable;
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private boolean shouldApplyDefaultEventListingOrder(Pageable pageable) {
        return pageable == null || pageable.isUnpaged() || !pageable.getSort().isSorted();
    }

    private Specification<Event> withUpcomingEventsFirstOrder(Specification<Event> specification) {
        return (root, query, cb) -> {
            Predicate predicate = specification == null ? cb.conjunction() : specification.toPredicate(root, query, cb);

            if (query != null && !isCountQuery(query)) {
                OffsetDateTime now = OffsetDateTime.now();
                Path<OffsetDateTime> startAt = root.get("startAt");
                Expression<Integer> upcomingBucket = cb.<Integer>selectCase()
                        .when(cb.greaterThanOrEqualTo(startAt, now), 0)
                        .otherwise(1);
                Expression<OffsetDateTime> upcomingOrder = cb.<OffsetDateTime>selectCase()
                        .when(cb.greaterThanOrEqualTo(startAt, now), startAt)
                        .otherwise(cb.nullLiteral(OffsetDateTime.class));
                Expression<OffsetDateTime> pastOrder = cb.<OffsetDateTime>selectCase()
                        .when(cb.lessThan(startAt, now), startAt)
                        .otherwise(cb.nullLiteral(OffsetDateTime.class));

                query.orderBy(
                        cb.asc(upcomingBucket),
                        cb.asc(upcomingOrder),
                        cb.desc(pastOrder),
                        cb.asc(root.get("id"))
                );
            }

            return predicate;
        };
    }

    private boolean isCountQuery(jakarta.persistence.criteria.CriteriaQuery<?> query) {
        Class<?> resultType = query.getResultType();
        return resultType == Long.class || resultType == long.class;
    }

    private String normalizeQuery(String q) {
        return q == null || q.isBlank() ? null : q.trim();
    }

    private EventTimeFilter defaultUpcoming(EventTimeFilter timeFilter) {
        return timeFilter == null ? EventTimeFilter.UPCOMING : timeFilter;
    }

    private EventTimeFilter defaultAll(EventTimeFilter timeFilter) {
        return timeFilter == null ? EventTimeFilter.ALL : timeFilter;
    }
}
