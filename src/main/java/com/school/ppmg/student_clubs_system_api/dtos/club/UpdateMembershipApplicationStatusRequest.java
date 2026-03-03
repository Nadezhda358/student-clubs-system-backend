package com.school.ppmg.student_clubs_system_api.dtos.club;

import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMembershipApplicationStatusRequest(
        @NotNull MembershipRequestStatus status
) {}
