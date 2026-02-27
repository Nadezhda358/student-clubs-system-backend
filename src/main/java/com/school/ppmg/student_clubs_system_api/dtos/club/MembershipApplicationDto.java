package com.school.ppmg.student_clubs_system_api.dtos.club;

import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;

import java.time.OffsetDateTime;

public record MembershipApplicationDto(
        Long id,
        Long clubId,
        String clubName,
        Long studentId,
        MembershipRequestStatus status,
        String motivationText,
        OffsetDateTime createdAt
) {}
