package com.school.ppmg.student_clubs_system_api.dtos.club;

public interface ClubWriteRequest {

    String name();

    String description();

    String scheduleText();

    String room();

    String contactEmail();

    String contactPhone();

    Boolean isActive();
}
