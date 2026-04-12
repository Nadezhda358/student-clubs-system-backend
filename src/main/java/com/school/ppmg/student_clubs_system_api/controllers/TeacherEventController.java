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
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher/events")
public class TeacherEventController {

    private final EventService eventService;

    @GetMapping
    public Page<EventListDto> getTeacherEvents(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) EventTimeFilter timeFilter,
            @RequestParam(required = false) EventStatus status,
            Pageable pageable
    ) {
        return eventService.getTeacherEvents(clubId, q, from, to, timeFilter, status, pageable);
    }

    @GetMapping("/{id}")
    public EventDto getTeacherEventById(@PathVariable Long id) {
        return eventService.getTeacherEventById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createTeacherEvent(@Valid @RequestBody UpsertEventDto dto) {
        return eventService.createTeacherEvent(dto);
    }

    @PutMapping("/{id}")
    public EventDto updateTeacherEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpsertEventDto dto
    ) {
        return eventService.updateTeacherEvent(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeacherEvent(@PathVariable Long id) {
        eventService.deleteTeacherEvent(id);
    }

    @PostMapping(value = "/{id}/main-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventDto uploadTeacherEventMainImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return eventService.updateTeacherMainImage(id, file);
    }

    @GetMapping("/{id}/participants")
    public Page<EventParticipationDto> getTeacherParticipants(
            @PathVariable Long id,
            @RequestParam(required = false) RegistrationStatus status,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return eventService.getTeacherParticipants(id, status, q, pageable);
    }

    @PatchMapping("/{eventId}/participants/{studentId}")
    public EventParticipationDto updateTeacherParticipationStatus(
            @PathVariable Long eventId,
            @PathVariable Long studentId,
            @Valid @RequestBody UpdateEventParticipationStatusRequest request
    ) {
        return eventService.updateTeacherParticipationStatus(eventId, studentId, request.status());
    }
}
