package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEventParticipationStatusRequest(
        @NotNull RegistrationStatus status
) {}
