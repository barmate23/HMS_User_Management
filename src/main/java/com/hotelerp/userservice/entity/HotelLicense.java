package com.hotelerp.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hotel_licenses", indexes = {
        @Index(name = "idx_license_key", columnList = "licenseKey", unique = true),
        @Index(name = "idx_client_email", columnList = "clientEmail"),
        @Index(name = "idx_license_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "licenseKey", nullable = false, unique = true, length = 100)
    private String licenseKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "clientEmail", nullable = false, length = 150)
    private String clientEmail;

    /** STARTER | PROFESSIONAL | ENTERPRISE */
    @Column(name = "tier", nullable = false, length = 50)
    private String tier;

    @Column(name = "maxRooms", nullable = false)
    private Integer maxRooms;

    @Column(name = "maxUsers", nullable = false)
    private Integer maxUsers;

    /** Comma-separated list of enabled feature modules */
    @Column(name = "enabledModules", columnDefinition = "TEXT")
    private String enabledModules;

    /** PENDING_ACTIVATION | ACTIVE | EXPIRED | REVOKED | RENEWED */
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "issuedAt", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "activatedAt")
    private LocalDateTime activatedAt;

    @Column(name = "expiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "portalUrl", length = 255)
    private String portalUrl;

    @Builder.Default
    @Column(name = "renewalCount")
    private Integer renewalCount = 0;

    @Column(name = "lastRenewalAt")
    private LocalDateTime lastRenewalAt;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (issuedAt == null) issuedAt = LocalDateTime.now();
        if (status == null) status = "PENDING_ACTIVATION";
        if (renewalCount == null) renewalCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
