package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.auth.TeacherInviteRequest;
import com.school.ppmg.student_clubs_system_api.dtos.auth.TeacherInviteResponse;
import com.school.ppmg.student_clubs_system_api.entities.user.TeacherInvite;
import com.school.ppmg.student_clubs_system_api.repositories.TeacherInviteRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import com.school.ppmg.student_clubs_system_api.services.email.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherInviteServiceTest {

    @Mock
    private TeacherInviteRepository teacherInviteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private TeacherInviteService teacherInviteService;

    @Test
    void createInvitesSkipsExistingUsersAndSendsInviteLink() {
        ReflectionTestUtils.setField(
                teacherInviteService,
                "inviteBaseUrl",
                "https://clubshub.example.com/register/teacher?lang=bg"
        );

        when(userRepository.existsByEmail("new.teacher@example.com")).thenReturn(false);
        when(userRepository.existsByEmail("existing.teacher@example.com")).thenReturn(true);
        when(tokenService.generateToken()).thenReturn("tok+123");
        when(tokenService.hashToken("tok+123")).thenReturn("hashed-token");
        when(teacherInviteRepository.save(any(TeacherInvite.class))).thenAnswer(invocation -> {
            TeacherInvite invite = invocation.getArgument(0);
            invite.setId(11L);
            return invite;
        });

        List<TeacherInviteResponse> responses = teacherInviteService.createInvites(
                new TeacherInviteRequest(List.of(" New.Teacher@example.com ", "existing.teacher@example.com"))
        );

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(11L);
        assertThat(responses.get(0).email()).isEqualTo("new.teacher@example.com");
        assertThat(responses.get(0).expiresAt()).isNotNull();

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendTeacherInvite(emailCaptor.capture(), linkCaptor.capture());

        assertThat(emailCaptor.getValue()).isEqualTo("new.teacher@example.com");
        assertThat(linkCaptor.getValue())
                .isEqualTo("https://clubshub.example.com/register/teacher?lang=bg&token=tok%2B123");
    }

    @Test
    void createInvitesRejectsBatchWhenEveryEmailAlreadyExists() {
        when(userRepository.existsByEmail("teacher@example.com")).thenReturn(true);

        assertThatThrownBy(() -> teacherInviteService.createInvites(
                new TeacherInviteRequest(List.of("teacher@example.com"))
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(409));
    }
}
