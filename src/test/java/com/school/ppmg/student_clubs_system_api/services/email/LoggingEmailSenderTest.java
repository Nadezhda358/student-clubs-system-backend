package com.school.ppmg.student_clubs_system_api.services.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class LoggingEmailSenderTest {

    @Test
    void sendTeacherInviteDoesNotThrow() {
        LoggingEmailSender sender = new LoggingEmailSender();

        assertThatCode(() -> sender.sendTeacherInvite("teacher@example.com", "https://example.com/register"))
                .doesNotThrowAnyException();
    }
}
