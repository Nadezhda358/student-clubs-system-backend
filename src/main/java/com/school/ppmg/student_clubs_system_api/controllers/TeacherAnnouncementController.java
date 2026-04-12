package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.announcement.AnnouncementDto;
import com.school.ppmg.student_clubs_system_api.dtos.announcement.UpsertAnnouncementDto;
import com.school.ppmg.student_clubs_system_api.services.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/teacher/announcements")
public class TeacherAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Page<AnnouncementDto> getTeacherAnnouncements(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            Pageable pageable
    ) {
        return announcementService.getTeacherAnnouncements(clubId, published, q, from, to, pageable);
    }

    @GetMapping("/{id}")
    public AnnouncementDto getTeacherAnnouncementById(@PathVariable Long id) {
        return announcementService.getTeacherAnnouncementById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementDto createTeacherAnnouncement(@Valid @RequestBody UpsertAnnouncementDto dto) {
        return announcementService.createTeacherAnnouncement(dto);
    }

    @PutMapping("/{id}")
    public AnnouncementDto updateTeacherAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody UpsertAnnouncementDto dto
    ) {
        return announcementService.updateTeacherAnnouncement(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeacherAnnouncement(@PathVariable Long id) {
        announcementService.deleteTeacherAnnouncement(id);
    }
}
