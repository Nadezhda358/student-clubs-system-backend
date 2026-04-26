package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.event.EventDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.EventListDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.EventParticipationDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.MyEventDto;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import com.school.ppmg.student_clubs_system_api.enums.EventTimeFilter;
import com.school.ppmg.student_clubs_system_api.enums.RegistrationStatus;
import com.school.ppmg.student_clubs_system_api.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    @GetMapping("/events")
    public Page<EventListDto> getPublicEvents(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) EventTimeFilter timeFilter,
            Pageable pageable
    ) {
        return eventService.getPublicEvents(clubId, q, from, to, timeFilter, pageable);
    }

    @GetMapping("/events/{id}")
    public EventDto getPublicEventById(@PathVariable Long id) {
        return eventService.getPublicById(id);
    }

    @GetMapping("/me/events/registered")
    @PreAuthorize("hasRole('STUDENT')")
    public Page<EventListDto> getMyRegisteredPublicEvents(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) EventTimeFilter timeFilter,
            Pageable pageable
    ) {
        return eventService.getMyRegisteredPublicEvents(clubId, q, from, to, timeFilter, pageable);
    }

    @PostMapping("/events/{id}/registrations")
    @PreAuthorize("hasRole('STUDENT')")
    public EventParticipationDto register(@PathVariable Long id) {
        return eventService.registerCurrentStudent(id);
    }

    @DeleteMapping("/events/{id}/registrations")
    @PreAuthorize("hasRole('STUDENT')")
    public EventParticipationDto cancelRegistration(@PathVariable Long id) {
        return eventService.cancelCurrentStudentRegistration(id);
    }

    @GetMapping("/me/events")
    @PreAuthorize("hasRole('STUDENT')")
    public Page<MyEventDto> getMyEvents(
            @RequestParam(required = false) RegistrationStatus registrationStatus,
            @RequestParam(required = false) EventStatus eventStatus,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EventTimeFilter timeFilter,
            Pageable pageable
    ) {
        return eventService.getMyEvents(registrationStatus, eventStatus, q, timeFilter, pageable);
    }
}
