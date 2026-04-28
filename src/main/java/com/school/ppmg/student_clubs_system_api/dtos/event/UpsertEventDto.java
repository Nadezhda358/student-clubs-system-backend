package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.enums.EventAudience;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record UpsertEventDto(
        @NotNull Long clubId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 5000) String description,
        @NotNull OffsetDateTime startAt,
        OffsetDateTime endAt,
        @Size(max = 200) String location,
        @Min(0) Integer capacity,
        OffsetDateTime registrationDeadline,
        @NotNull EventStatus status,
        @NotNull EventAudience audience
) {

    @AssertTrue(message = "Крайният час трябва да е на или след началния")
    public boolean isEndAfterStart() {
        return endAt == null || startAt == null || !endAt.isBefore(startAt);
    }

    @AssertTrue(message = "Крайният срок за записване трябва да е на или преди началото")
    public boolean isDeadlineValid() {
        return registrationDeadline == null || startAt == null || !registrationDeadline.isAfter(startAt);
    }
}
