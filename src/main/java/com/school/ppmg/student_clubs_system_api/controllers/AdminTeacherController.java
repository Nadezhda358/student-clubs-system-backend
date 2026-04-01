package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.auth.UserDto;
import com.school.ppmg.student_clubs_system_api.services.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public List<UserDto> getAllTeachers() {
        return teacherService.getAllTeachers();
    }
}
