package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.event.EventDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.EventListDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.EventParticipationDto;
import com.school.ppmg.student_clubs_system_api.dtos.event.UpdateEventParticipationStatusRequest;
import com.school.ppmg.student_clubs_system_api.dtos.event.UpsertEventDto;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import com.school.ppmg.student_clubs_system_api.enums.EventTimeFilter;
import com.school.ppmg.student_clubs_system_api.enums.RegistrationStatus;
import com.school.ppmg.student_clubs_system_api.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping("/events")
    public Page<EventListDto> getAdminEvents(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) EventTimeFilter timeFilter,
            @RequestParam(required = false) EventStatus status,
            Pageable pageable
    ) {
        return eventService.getAdminEvents(clubId, q, from, to, timeFilter, status, pageable);
    }

    @GetMapping("/events/{id}")
    public EventDto getAdminEventById(@PathVariable Long id) {
        return eventService.getAdminEventById(id);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createAdminEvent(@Valid @RequestBody UpsertEventDto dto) {
        return eventService.createAdminEvent(dto);
    }

    @PutMapping("/events/{id}")
    public EventDto updateAdminEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpsertEventDto dto
    ) {
        return eventService.updateAdminEvent(id, dto);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAdminEvent(@PathVariable Long id) {
        eventService.deleteAdminEvent(id);
    }

    @GetMapping("/events/{id}/participants")
    public Page<EventParticipationDto> getAdminParticipantsForEvent(
            @PathVariable Long id,
            @RequestParam(required = false) RegistrationStatus status,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return eventService.getAdminParticipantsForEvent(id, status, q, pageable);
    }

    @PatchMapping("/events/{eventId}/participants/{studentId}")
    public EventParticipationDto updateAdminParticipationStatus(
            @PathVariable Long eventId,
            @PathVariable Long studentId,
            @Valid @RequestBody UpdateEventParticipationStatusRequest request
    ) {
        return eventService.updateAdminParticipationStatus(eventId, studentId, request.status());
    }

    @GetMapping("/event-participations")
    public Page<EventParticipationDto> getAdminParticipations(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) RegistrationStatus registrationStatus,
            @RequestParam(required = false) EventStatus eventStatus,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EventTimeFilter timeFilter,
            Pageable pageable
    ) {
        return eventService.getAdminParticipations(
                clubId,
                eventId,
                registrationStatus,
                eventStatus,
                q,
                timeFilter,
                pageable
        );
    }
}
