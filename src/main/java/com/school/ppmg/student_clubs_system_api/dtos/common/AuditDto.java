package com.school.ppmg.student_clubs_system_api.dtos.common;

import java.time.OffsetDateTime;

public record AuditDto(
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt
) {}