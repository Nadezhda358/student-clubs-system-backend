package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.auth.LoginRequest;
import com.school.ppmg.student_clubs_system_api.dtos.auth.LoginResponse;
import com.school.ppmg.student_clubs_system_api.dtos.auth.RegisterStudentRequest;
import com.school.ppmg.student_clubs_system_api.dtos.auth.RegisterTeacherRequest;
import com.school.ppmg.student_clubs_system_api.dtos.auth.UserDto;
import com.school.ppmg.student_clubs_system_api.services.AuthService;
import com.school.ppmg.student_clubs_system_api.services.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        return registrationService.registerStudent(request);
    }

    @PostMapping("/register/teacher")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerTeacher(@Valid @RequestBody RegisterTeacherRequest request) {
        return registrationService.registerTeacher(request);
    }
}
