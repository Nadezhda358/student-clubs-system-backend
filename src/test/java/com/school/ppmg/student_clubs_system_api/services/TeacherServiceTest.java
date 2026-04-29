package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.auth.UserDto;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TeacherServiceTest {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getAllTeachersFiltersByQueryAndSortsByName() {
        String marker = "teacher-filter-" + UUID.randomUUID();

        User laterTeacher = createUser("Зорница", "Петрова", "b-" + marker + "@example.com", UserRole.TEACHER);
        User earlierTeacher = createUser("Анелия", "Стоянова", "a-" + marker + "@example.com", UserRole.TEACHER);
        createUser("Ученик", "Тестов", "student-" + marker + "@example.com", UserRole.STUDENT);

        Page<UserDto> page = teacherService.getAllTeachers(marker, PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(UserDto::id)
                .containsExactly(earlierTeacher.getId(), laterTeacher.getId());
        assertThat(page.getContent())
                .extracting(UserDto::role)
                .containsOnly("TEACHER");
    }

    private User createUser(String firstName, String lastName, String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("password-hash");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }
}
