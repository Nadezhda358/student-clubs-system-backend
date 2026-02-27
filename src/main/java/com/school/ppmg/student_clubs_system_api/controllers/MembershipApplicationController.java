package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.club.CreateMembershipApplicationRequest;
import com.school.ppmg.student_clubs_system_api.dtos.club.MembershipApplicationDto;
import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;
import com.school.ppmg.student_clubs_system_api.services.ClubMembershipRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MembershipApplicationController {

    private final ClubMembershipRequestService clubMembershipRequestService;

    @PostMapping("/clubs/{clubId}/membership-applications")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public MembershipApplicationDto apply(
            @PathVariable Long clubId,
            @Valid @RequestBody CreateMembershipApplicationRequest request
    ) {
        return clubMembershipRequestService.apply(clubId, request);
    }

    @GetMapping("/me/membership-applications")
    @PreAuthorize("hasRole('STUDENT')")
    public List<MembershipApplicationDto> getMyApplications(
            @RequestParam(required = false) MembershipRequestStatus status
    ) {
        return clubMembershipRequestService.getMyApplications(status);
    }
}
