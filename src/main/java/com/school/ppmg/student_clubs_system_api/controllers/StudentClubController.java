package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.club.ClubListDto;
import com.school.ppmg.student_clubs_system_api.services.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/clubs")
public class StudentClubController {

    private final ClubService clubService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Page<ClubListDto> getMyClubs(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return clubService.getMyClubs(active, q, pageable);
    }
}
