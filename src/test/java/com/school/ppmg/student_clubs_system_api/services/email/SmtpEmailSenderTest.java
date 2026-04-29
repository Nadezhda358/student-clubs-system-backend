package com.school.ppmg.student_clubs_system_api.services.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpEmailSender smtpEmailSender;

    @BeforeEach
    void setUp() {
        smtpEmailSender = new SmtpEmailSender(mailSender);
        ReflectionTestUtils.setField(smtpEmailSender, "fromEmail", "no-reply@example.com");
        ReflectionTestUtils.setField(smtpEmailSender, "inviteSubject", "Покана за учителски профил");
    }

    @Test
    void sendTeacherInviteBuildsAndSendsMimeMessage() throws Exception {
        MimeMessage message = new MimeMessage((jakarta.mail.Session) null);
        when(mailSender.createMimeMessage()).thenReturn(message);

        smtpEmailSender.sendTeacherInvite(
                "teacher@example.com",
                "https://clubshub.example.com/register/teacher?token=abc123"
        );

        verify(mailSender).send(message);
        assertThat(message.getSubject()).isEqualTo("Покана за учителски профил");
        assertThat(message.getFrom()[0].toString()).contains("no-reply@example.com");
        assertThat(message.getAllRecipients()[0].toString()).contains("teacher@example.com");
    }
}
