package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.auth.TeacherInviteRequest;
import com.school.ppmg.student_clubs_system_api.dtos.auth.TeacherInviteResponse;
import com.school.ppmg.student_clubs_system_api.entities.user.TeacherInvite;
import com.school.ppmg.student_clubs_system_api.repositories.TeacherInviteRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import com.school.ppmg.student_clubs_system_api.services.email.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TeacherInviteService {

    private final TeacherInviteRepository teacherInviteRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailSender emailSender;

    @Value("${app.teacher-invite.base-url:http://localhost:8081/teacher/register}")
    private String inviteBaseUrl;

    @Transactional
    public TeacherInviteResponse createInvite(TeacherInviteRequest request) {
        String email = request.email().trim();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        String token = tokenService.generateToken();
        String tokenHash = tokenService.hashToken(token);

        TeacherInvite invite = new TeacherInvite();
        invite.setEmail(email);
        invite.setTokenHash(tokenHash);
        invite.setExpiresAt(OffsetDateTime.now().plusHours(48));

        TeacherInvite saved = teacherInviteRepository.save(invite);

        String inviteLink = buildInviteLink(token);
        emailSender.sendTeacherInvite(email, inviteLink);

        return new TeacherInviteResponse(saved.getId(), saved.getEmail(), saved.getExpiresAt());
    }

    private String buildInviteLink(String token) {
        String encoded = URLEncoder.encode(token, StandardCharsets.UTF_8);
        if (inviteBaseUrl.contains("?")) {
            return inviteBaseUrl + "&token=" + encoded;
        }
        return inviteBaseUrl + "?token=" + encoded;
    }
}
