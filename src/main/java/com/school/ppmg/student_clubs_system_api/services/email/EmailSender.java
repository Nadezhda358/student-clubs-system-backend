package com.school.ppmg.student_clubs_system_api.services.email;

public interface EmailSender {
    void sendTeacherInvite(String email, String inviteLink);
}
