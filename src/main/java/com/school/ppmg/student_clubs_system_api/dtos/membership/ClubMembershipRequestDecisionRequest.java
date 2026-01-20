package com.school.ppmg.student_clubs_system_api.dtos.membership;

import jakarta.validation.constraints.Size;

public record ClubMembershipRequestDecisionRequest(
        // status is chosen by endpoint (approve/reject) OR include it if you prefer
        @Size(max = 2000) String decisionNote
) {}

