package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.auth.TeacherInviteRequest;
import com.school.ppmg.student_clubs_system_api.dtos.auth.TeacherInviteResponse;
import com.school.ppmg.student_clubs_system_api.services.TeacherInviteService;
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
@RequestMapping("/api/admin")
public class AdminTeacherInviteController {

    private final TeacherInviteService teacherInviteService;

    @PostMapping("/teacher-invites")
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherInviteResponse createInvite(@Valid @RequestBody TeacherInviteRequest request) {
        return teacherInviteService.createInvite(request);
    }
}
