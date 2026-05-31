package com.hotelerp.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_username", columnList = "username"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_severity", columnList = "severity"),
        @Index(name = "idx_audit_module", columnList = "module")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "role", length = 100)
    private String role;

    @Column(name = "activity", nullable = false, columnDefinition = "TEXT")
    private String activity;

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "severity", length = 20)
    private String severity; // INFO, WARNING, CRITICAL

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
