package com.school.ppmg.student_clubs_system_api.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Profile("!dev")
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.teacher-invite.from-email")
    private String fromEmail;

    @Value("${app.teacher-invite.subject:Teacher account invitation}")
    private String inviteSubject;


    @Override
    public void sendTeacherInvite(String email, String inviteLink) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    msg,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            helper.setTo(email);
            helper.setFrom(fromEmail);
            helper.setSubject(inviteSubject);

            String plain = """
                    Teacher account invitation

                    You have been invited to create a teacher account on ClubsHub.
                    Complete registration here:
                    %s
                    """.formatted(inviteLink);

            String logoCid = "clubshubLogo"; // no spaces
            String html = buildTeacherInviteHtml(inviteLink, logoCid);

            // IMPORTANT: set both plain + html
            helper.setText(plain, html);

            // IMPORTANT: addInline AFTER setText (most reliable across clients)
            helper.addInline(logoCid, new ClassPathResource("static/assets/logo.png"), "image/png");

            mailSender.send(msg);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send teacher invite email", ex);
        }
    }

    private String buildTeacherInviteHtml(String inviteLink, String logoCid) {
        // avoid % formatting entirely to prevent crashes
        return """
<!doctype html>
<html>
<body style="margin:0;background:#F5F0FA;font-family:Arial,Helvetica,sans-serif;">
  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="padding:28px 12px;background:#F5F0FA;">
    <tr><td align="center">
      <table role="presentation" width="640" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:18px;overflow:hidden;">
        
        <tr>
          <td style="padding:18px 22px;background:#F7F2FF;">
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0">
              <tr>
                <td style="vertical-align:middle;">
                  <img src="cid:{{LOGO_CID}}" alt="ClubsHub" style="height:40px;display:block;">
                </td>
                <td align="right" style="vertical-align:middle;">
                  <span style="display:inline-block;padding:8px 12px;border-radius:999px;background:#F2D34C;color:#2E2225;font-size:12px;letter-spacing:.7px;">
                    STUDENT LIFE
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <tr>
          <td style="padding:26px 22px;">
            <h1 style="margin:0 0 10px 0;font-size:22px;color:#2E2225;">Teacher account invitation</h1>
            <p style="margin:0 0 18px 0;font-size:15px;line-height:1.6;color:#3A2A2F;">
              You’ve been invited to create a <b>teacher account</b> on <b>ClubsHub</b>.
            </p>

            <a href="{{INVITE_LINK}}"
               style="display:inline-block;background:#754FAB;color:#ffffff;text-decoration:none;
                      padding:12px 18px;border-radius:12px;font-size:15px;font-weight:700;">
              Complete registration
            </a>

            <p style="margin:18px 0 6px 0;font-size:12.5px;color:#5A4A50;">
              If the button doesn’t work, copy and paste this link:
            </p>
            <p style="margin:0;font-size:12.5px;word-break:break-all;">
              <a href="{{INVITE_LINK}}" style="color:#3B2A72;">{{INVITE_LINK}}</a>
            </p>
          </td>
        </tr>

        <tr>
          <td style="padding:14px 22px;background:#F7F2FF;font-size:12px;color:#5A4A50;">
            © {{YEAR}} ClubsHub · Student Clubs & Campus Events
          </td>
        </tr>

      </table>
    </td></tr>
  </table>
</body>
</html>
"""
                .replace("{{LOGO_CID}}", logoCid)
                .replace("{{INVITE_LINK}}", inviteLink)
                .replace("{{YEAR}}", String.valueOf(java.time.Year.now().getValue()));
    }
}



