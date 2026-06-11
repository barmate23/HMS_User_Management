package com.hotelerp.userservice.service;

import com.hotelerp.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnClass(JavaMailSender.class)
public class UserCredentialEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreplyhelixion@gmail.com}")
    private String fromAddress;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public boolean sendTemporaryPassword(User user, String temporaryPassword) {
        if (!mailEnabled || !StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)) {
            log.warn("Mail is disabled or not configured. Skipping temporary password email for {}", user.getEmail());
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(user.getEmail());
            message.setSubject("HMS Cloud temporary password");
            message.setText("""
                    Hello %s,

                    Your HMS Cloud login has been created or reset.

                    Username: %s
                    Temporary password: %s

                    Sign in with this temporary password and set your custom password immediately.

                    Regards,
                    HMS Cloud
                    """.formatted(user.getFullName(), user.getUsername(), temporaryPassword));
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send temporary password email to {}", user.getEmail(), ex);
            return false;
        }
    }

    public boolean sendPasswordResetOtp(User user, String resetCode, java.time.LocalDateTime expiresAt) {
        if (!mailEnabled || !StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)) {
            log.warn("Mail is disabled or not configured. Skipping password reset OTP email for {}", user.getEmail());
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(user.getEmail());
            message.setSubject("HMS Cloud password reset OTP");
            message.setText("""
                    Hello %s,

                    Use this one-time password to reset your HMS Cloud password:

                    OTP: %s

                    This code expires at %s. If you did not request this reset, contact your administrator.

                    Regards,
                    HMS Cloud
                    """.formatted(user.getFullName(), resetCode, expiresAt));
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send password reset OTP email to {}", user.getEmail(), ex);
            return false;
        }
    }
}
