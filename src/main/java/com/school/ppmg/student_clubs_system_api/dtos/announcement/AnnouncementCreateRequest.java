package com.school.ppmg.student_clubs_system_api.dtos.announcement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnouncementCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 8000) String body,
        boolean isPublished
) {}
