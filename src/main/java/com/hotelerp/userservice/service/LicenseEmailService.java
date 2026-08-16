package com.hotelerp.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnClass(JavaMailSender.class)
public class LicenseEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreplyhelixion@gmail.com}")
    private String fromAddress;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean sendOnboardingLicenseEmail(
            String clientEmail,
            String hotelName,
            String adminUsername,
            String temporaryPassword,
            String licenseKey,
            String portalUrl,
            java.time.LocalDateTime expiresAt,
            String tier,
            Integer maxRooms
    ) {
        if (!mailEnabled || !StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)) {
            log.warn("Mail sender disabled or credentials not configured. Skipping onboarding email for {}", clientEmail);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(clientEmail);
            message.setSubject("Welcome to HMS Cloud - Your Activation & License Details for " + hotelName);
            
            String body = """
                    Welcome to HMS Cloud!
                    
                    Thank you for choosing HMS Cloud. Your payment confirmation has been processed, and your property instance is ready for activation.
                    
                    --- HOTEL & SUBSCRIPTION DETAILS ---
                    Hotel Name: %s
                    License Tier: %s
                    Room Quota: %d Rooms
                    License Expires On: %s
                    
                    --- CLOUD HMS PORTAL ACCESS ---
                    Portal URL: %s
                    Default Admin Username: %s
                    Temporary Password: %s
                    
                    --- LICENSE ACTIVATION KEY ---
                    License Key: %s
                    
                    INSTRUCTIONS:
                    1. Click on the Portal URL link above to navigate to your HMS Cloud instance.
                    2. Sign in using your default Admin Username and Temporary Password.
                    3. Input the License Key provided above to activate your HMS Cloud portal.
                    
                    Note: Upon your first sign-in, you will be prompted to update your temporary password for security.
                    
                    Best Regards,
                    HMS Cloud Licensing & Onboarding Team
                    """.formatted(
                            hotelName,
                            tier,
                            maxRooms,
                            expiresAt.format(DATE_FORMATTER),
                            portalUrl,
                            adminUsername,
                            temporaryPassword,
                            licenseKey
                    );

            message.setText(body);
            mailSender.send(message);
            log.info("Successfully sent HMS onboarding license email to {}", clientEmail);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send onboarding license email to {}", clientEmail, ex);
            return false;
        }
    }

    public boolean sendLicenseRenewalEmail(
            String clientEmail,
            String hotelName,
            String newLicenseKey,
            java.time.LocalDateTime newExpiresAt,
            String portalUrl,
            String tier,
            Integer maxRooms
    ) {
        if (!mailEnabled || !StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)) {
            log.warn("Mail sender disabled or credentials not configured. Skipping license renewal email for {}", clientEmail);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(clientEmail);
            message.setSubject("HMS Cloud Subscription Renewed - New License Key for " + hotelName);
            
            String body = """
                    Hello %s Team,
                    
                    Your HMS Cloud subscription renewal has been processed successfully!
                    
                    --- UPDATED SUBSCRIPTION DETAILS ---
                    License Tier: %s
                    Room Quota: %d Rooms
                    New Expiration Date: %s
                    
                    --- RENEWAL LICENSE KEY ---
                    New License Key: %s
                    
                    INSTRUCTIONS TO RENEW:
                    1. Access your HMS Cloud Portal at: %s
                    2. Go to Setup / License Management section.
                    3. Enter your New License Key above and click 'Activate / Renew'.
                    
                    Thank you for continuing with HMS Cloud!
                    
                    Best Regards,
                    HMS Cloud Licensing Team
                    """.formatted(
                            hotelName,
                            tier,
                            maxRooms,
                            newExpiresAt.format(DATE_FORMATTER),
                            newLicenseKey,
                            portalUrl
                    );

            message.setText(body);
            mailSender.send(message);
            log.info("Successfully sent HMS license renewal email to {}", clientEmail);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send license renewal email to {}", clientEmail, ex);
            return false;
        }
    }
}
