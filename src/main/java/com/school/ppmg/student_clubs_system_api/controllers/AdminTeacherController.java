package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.auth.UserDto;
import com.school.ppmg.student_clubs_system_api.services.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public Page<UserDto> getAllTeachers(
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return teacherService.getAllTeachers(q, pageable);
    }
}
