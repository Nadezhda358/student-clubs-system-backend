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
import java.util.ArrayList;
import java.util.List;

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
    public List<TeacherInviteResponse> createInvites(TeacherInviteRequest request) {

        List<TeacherInviteResponse> responses = new ArrayList<>();

        for (String rawEmail : request.emails()) {
            String email = rawEmail.trim().toLowerCase();

            if (userRepository.existsByEmail(email)) {
                continue;
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

            responses.add(
                    new TeacherInviteResponse(
                            saved.getId(),
                            saved.getEmail(),
                            saved.getExpiresAt()
                    )
            );
        }

        if (responses.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No invites were created"
            );
        }

        return responses;
    }


    private String buildInviteLink(String token) {
        String encoded = URLEncoder.encode(token, StandardCharsets.UTF_8);
        if (inviteBaseUrl.contains("?")) {
            return inviteBaseUrl + "&token=" + encoded;
        }
        return inviteBaseUrl + "?token=" + encoded;
    }
}
