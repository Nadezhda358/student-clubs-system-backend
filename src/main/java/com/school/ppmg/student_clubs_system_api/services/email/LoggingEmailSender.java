package com.school.ppmg.student_clubs_system_api.services.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("dev")
public class LoggingEmailSender implements EmailSender {

    @Override
    public void sendTeacherInvite(String email, String inviteLink) {
        log.info("[DEV] Teacher invite for {}: {}", email, inviteLink);
    }
}
