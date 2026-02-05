package com.school.ppmg.student_clubs_system_api.dtos.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserDto user
) {}
