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
@RequestMapping("/api/admin/announcements")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Page<AnnouncementDto> getAdminAnnouncements(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            Pageable pageable
    ) {
        return announcementService.getAdminAnnouncements(clubId, published, q, from, to, pageable);
    }

    @GetMapping("/{id}")
    public AnnouncementDto getAdminAnnouncementById(@PathVariable Long id) {
        return announcementService.getAdminAnnouncementById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementDto createAdminAnnouncement(@Valid @RequestBody UpsertAnnouncementDto dto) {
        return announcementService.createAdminAnnouncement(dto);
    }

    @PutMapping("/{id}")
    public AnnouncementDto updateAdminAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody UpsertAnnouncementDto dto
    ) {
        return announcementService.updateAdminAnnouncement(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAdminAnnouncement(@PathVariable Long id) {
        announcementService.deleteAdminAnnouncement(id);
    }
}
