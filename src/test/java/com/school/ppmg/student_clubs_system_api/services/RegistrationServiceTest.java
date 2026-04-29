package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.auth.RegisterStudentRequest;
import com.school.ppmg.student_clubs_system_api.dtos.auth.RegisterTeacherRequest;
import com.school.ppmg.student_clubs_system_api.dtos.auth.UserDto;
import com.school.ppmg.student_clubs_system_api.entities.user.TeacherInvite;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.TeacherInviteRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeacherInviteRepository teacherInviteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registerStudentTrimsEmailAndAssignsStudentRole() {
        RegisterStudentRequest request = new RegisterStudentRequest(
                " student@example.com ",
                "Parola2026!",
                "Петър",
                "Иванов",
                10,
                "Б"
        );

        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Parola2026!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(15L);
            return user;
        });

        UserDto response = registrationService.registerStudent(request);

        assertThat(response.id()).isEqualTo(15L);
        assertThat(response.email()).isEqualTo("student@example.com");
        assertThat(response.role()).isEqualTo("STUDENT");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("student@example.com");
        assertThat(saved.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(saved.getGrade()).isEqualTo(10);
        assertThat(saved.getClassName()).isEqualTo("Б");
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void registerTeacherUsesInviteAndMarksItUsed() {
        TeacherInvite invite = new TeacherInvite();
        invite.setId(3L);
        invite.setEmail("teacher@example.com");
        invite.setTokenHash("hashed-token");
        invite.setExpiresAt(OffsetDateTime.now().plusHours(2));

        when(tokenService.hashToken("invite-token")).thenReturn("hashed-token");
        when(teacherInviteRepository.findByTokenHashForUpdate("hashed-token")).thenReturn(Optional.of(invite));
        when(userRepository.existsByEmail("teacher@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Parola2026!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(21L);
            return user;
        });

        UserDto response = registrationService.registerTeacher(
                new RegisterTeacherRequest(" invite-token ", "Parola2026!", "Милена", "Тодорова")
        );

        assertThat(response.id()).isEqualTo(21L);
        assertThat(response.email()).isEqualTo("teacher@example.com");
        assertThat(response.role()).isEqualTo("TEACHER");
        assertThat(invite.getUsedAt()).isNotNull();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.TEACHER);
        verify(teacherInviteRepository).save(invite);
    }

    @Test
    void registerTeacherRejectsExpiredInvite() {
        TeacherInvite invite = new TeacherInvite();
        invite.setEmail("teacher@example.com");
        invite.setTokenHash("expired-token");
        invite.setExpiresAt(OffsetDateTime.now().minusMinutes(1));

        when(tokenService.hashToken("expired-token")).thenReturn("expired-token");
        when(teacherInviteRepository.findByTokenHashForUpdate("expired-token")).thenReturn(Optional.of(invite));

        assertThatThrownBy(() -> registrationService.registerTeacher(
                new RegisterTeacherRequest("expired-token", "Parola2026!", "Мария", "Иванова")
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(400));
    }
}
