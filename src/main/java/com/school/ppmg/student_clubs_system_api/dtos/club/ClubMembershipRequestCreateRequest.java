package com.school.ppmg.student_clubs_system_api.dtos.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClubMembershipRequestCreateRequest(
        @NotBlank @Size(max = 2000) String message
) {}
