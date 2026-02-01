package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.club.ClubDto;
import com.school.ppmg.student_clubs_system_api.dtos.club.ClubListDto;
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
            Pageable pageable
    ) {
        return clubService.getAll(active, pageable);
    }

    @GetMapping("/{id}")
    public ClubDto getById(@PathVariable Long id) {
        return clubService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClubDto create(@Valid @RequestBody UpsertClubDto dto) {
        return clubService.create(dto);
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

    @PostMapping(value = "/{id}/main-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClubDto uploadMainImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return clubService.updateMainImage(id, file);
    }
}
