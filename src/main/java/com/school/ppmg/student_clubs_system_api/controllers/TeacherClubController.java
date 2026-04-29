package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.club.ClubDto;
import com.school.ppmg.student_clubs_system_api.dtos.club.ClubListDto;
import com.school.ppmg.student_clubs_system_api.dtos.club.ManageClubDto;
import com.school.ppmg.student_clubs_system_api.services.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher/clubs")
public class TeacherClubController {

    private final ClubService clubService;

    @GetMapping
    public Page<ClubListDto> getManagedClubs(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return clubService.getManagedClubs(active, q, pageable);
    }

    @GetMapping("/{id}")
    public ClubDto getManagedClubById(@PathVariable Long id) {
        return clubService.getManagedById(id);
    }

    @PutMapping("/{id}")
    public ClubDto updateManagedClub(
            @PathVariable Long id,
            @Valid @RequestBody ManageClubDto dto
    ) {
        return clubService.updateManagedClub(id, dto);
    }

    @PostMapping(value = "/{id}/main-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClubDto uploadManagedClubMainImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return clubService.updateManagedMainImage(id, file);
    }

    @PostMapping(value = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClubDto addManagedClubMedia(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files
    ) {
        return clubService.addManagedMedia(id, files);
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeManagedClubMedia(
            @PathVariable Long id,
            @PathVariable Long mediaId
    ) {
        clubService.removeManagedMedia(id, mediaId);
    }
}
