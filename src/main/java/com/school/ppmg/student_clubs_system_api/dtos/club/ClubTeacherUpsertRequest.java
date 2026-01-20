package com.school.ppmg.student_clubs_system_api.dtos.club;

import jakarta.validation.constraints.NotNull;

public record ClubTeacherUpsertRequest(
        @NotNull Long teacherUserId,
        boolean isPrimary
) {}