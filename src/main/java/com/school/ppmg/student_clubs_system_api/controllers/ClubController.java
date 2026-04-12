package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.club.ClubDto;
import com.school.ppmg.student_clubs_system_api.dtos.club.ClubListDto;
import com.school.ppmg.student_clubs_system_api.dtos.club.CreateClubOptions;
import com.school.ppmg.student_clubs_system_api.dtos.club.AddClubTeachersRequest;
import com.school.ppmg.student_clubs_system_api.dtos.club.CreateClubDto;
import com.school.ppmg.student_clubs_system_api.dtos.club.CreateClubRequest;
import com.school.ppmg.student_clubs_system_api.dtos.club.UpsertClubDto;
import com.school.ppmg.student_clubs_system_api.services.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    @GetMapping
    public Page<ClubListDto> getAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return clubService.getAll(active, q, pageable);
    }

    @GetMapping("/{id}")
    public ClubDto getById(@PathVariable Long id) {
        return clubService.getById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ClubDto create(@Valid @RequestBody CreateClubDto dto) {
        return clubService.create(dto);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ClubDto createMultipart(@Valid @ModelAttribute CreateClubRequest request) {
        return clubService.create(
                request.toCreateClubDto(),
                new CreateClubOptions(
                        request.getMainImage(),
                        request.getMediaFiles()
                )
        );
    }

    @PutMapping("/{id}")
    public ClubDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpsertClubDto dto
    ) {
        return clubService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        clubService.delete(id);
    }

    @PostMapping("/{id}/teachers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addTeachers(
            @PathVariable Long id,
            @Valid @RequestBody AddClubTeachersRequest request
    ) {
        clubService.addTeachers(id, request.teacherIds());
    }

    @DeleteMapping("/{id}/teachers/{teacherId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTeacher(
            @PathVariable Long id,
            @PathVariable Long teacherId
    ) {
        clubService.removeTeacher(id, teacherId);
    }

    @PostMapping(value = "/{id}/main-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClubDto uploadMainImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return clubService.updateMainImage(id, file);
    }
}
