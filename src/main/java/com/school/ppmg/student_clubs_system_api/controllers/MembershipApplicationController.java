package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.club.CreateMembershipApplicationRequest;
import com.school.ppmg.student_clubs_system_api.dtos.club.MembershipApplicationDto;
import com.school.ppmg.student_clubs_system_api.dtos.club.UpdateMembershipApplicationStatusRequest;
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

    @GetMapping("/admin/membership-applications")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MembershipApplicationDto> adminGetAllApplications(
            @RequestParam(required = false) MembershipRequestStatus status,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) String q
    ) {
        return clubMembershipRequestService.adminGetAll(status, clubId, q);
    }

    @PostMapping("/admin/membership-applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MembershipApplicationDto adminUpdateApplicationStatus(
            @PathVariable Long id,
            @RequestBody UpdateMembershipApplicationStatusRequest request
    ) {
        return clubMembershipRequestService.adminUpdateStatus(id, request == null ? null : request.status());
    }
}
