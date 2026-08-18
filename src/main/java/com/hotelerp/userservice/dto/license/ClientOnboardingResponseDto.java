package com.hotelerp.userservice.dto.license;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientOnboardingResponseDto {

    private Long hotelId;
    private String hotelName;
    private String hotelEmail;

    // Admin Credentials
    private Long adminUserId;
    private String adminFullName;
    private String adminUsername;
    private String adminEmail;
    private String temporaryPassword;

    // License Info
    private String licenseKey;
    private String licenseStatus;
    private String tier;
    private Integer maxRooms;
    private Integer maxUsers;
    private String enabledModules;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String portalUrl;
    private Boolean emailSent;
    private String message;
}
