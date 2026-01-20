package com.school.ppmg.student_clubs_system_api.dtos.membership;

import com.school.ppmg.student_clubs_system_api.dtos.user.UserSummaryDto;
import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;

import java.time.OffsetDateTime;

public record ClubMembershipRequestDto(
        Long id,
        Long clubId,
        Long studentUserId,
        UserSummaryDto student,

        MembershipRequestStatus status,
        String message,

        Long decidedBy,
        UserSummaryDto decidedByUser,
        OffsetDateTime decidedAt,
        String decisionNote
) {}

