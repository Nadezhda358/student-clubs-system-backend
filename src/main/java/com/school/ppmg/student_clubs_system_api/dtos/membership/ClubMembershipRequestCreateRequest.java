package com.school.ppmg.student_clubs_system_api.dtos.membership;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClubMembershipRequestCreateRequest(
        @NotBlank @Size(max = 2000) String message
) {}
