package com.school.ppmg.student_clubs_system_api.dtos.club;

import jakarta.validation.constraints.Size;

public record CreateMembershipApplicationRequest(
        @Size(max = 2000) String motivationText
) {}
