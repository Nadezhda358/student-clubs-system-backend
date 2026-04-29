package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.auth.LoginRequest;
import com.school.ppmg.student_clubs_system_api.dtos.auth.LoginResponse;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import com.school.ppmg.student_clubs_system_api.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginReturnsBearerTokenAndUserDto() {
        User user = createUser(7L, "student@example.com", UserRole.STUDENT);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Parola2026!", "stored-hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(new LoginRequest("student@example.com", "Parola2026!"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.user().email()).isEqualTo("student@example.com");
        assertThat(response.user().role()).isEqualTo("STUDENT");
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = createUser(7L, "student@example.com", UserRole.STUDENT);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("student@example.com", "wrong-password")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(401));
    }

    @Test
    void getCurrentUserRejectsAnonymousPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of())
        );

        assertThatThrownBy(authService::getCurrentUser)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(401));
    }

    @Test
    void getCurrentUserLoadsUserByAuthenticatedEmail() {
        User user = createUser(9L, "teacher@example.com", UserRole.TEACHER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("teacher@example.com", null, List.of())
        );
        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(user));

        User currentUser = authService.getCurrentUser();

        assertThat(currentUser.getId()).isEqualTo(9L);
        assertThat(currentUser.getEmail()).isEqualTo("teacher@example.com");
    }

    private User createUser(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash("stored-hash");
        user.setFirstName("Тест");
        user.setLastName("Потребител");
        user.setRole(role);
        return user;
    }
}
