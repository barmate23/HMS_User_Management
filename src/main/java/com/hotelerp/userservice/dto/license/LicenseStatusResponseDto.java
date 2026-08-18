package com.hotelerp.userservice.dto.license;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseStatusResponseDto {

    private Long hotelId;
    private String hotelName;
    private String clientEmail;
    private String licenseKey;
    private String status; // PENDING_ACTIVATION, ACTIVE, EXPIRED, REVOKED
    private Boolean isActive;
    private String tier;
    private Integer maxRooms;
    private Integer maxUsers;
    private List<String> enabledModules;
    private LocalDateTime issuedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private Long daysRemaining;
    private String message;
}
