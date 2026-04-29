package com.school.ppmg.student_clubs_system_api.dtos.report;

public record AdminClubParticipantsByClubDto(
        Long clubId,
        String clubName,
        Boolean active,
        Long participantsCount
) {}
