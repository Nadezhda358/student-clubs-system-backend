package com.school.ppmg.student_clubs_system_api.dtos.membership;

import com.school.ppmg.student_clubs_system_api.dtos.user.UserSummaryDto;
import com.school.ppmg.student_clubs_system_api.enums.MembershipStatus;

import java.time.OffsetDateTime;

public record ClubMembershipDto(
        Long clubId,
        Long studentUserId,
        UserSummaryDto student,
        MembershipStatus status,
        OffsetDateTime joinedAt,
        OffsetDateTime leftAt
) {}