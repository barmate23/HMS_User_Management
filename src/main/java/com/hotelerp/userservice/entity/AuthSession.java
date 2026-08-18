package com.hotelerp.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_sessions", indexes = {
        @Index(name = "idx_auth_session_access_jti", columnList = "accessTokenId", unique = true),
        @Index(name = "idx_auth_session_refresh_jti", columnList = "refreshTokenId", unique = true),
        @Index(name = "idx_auth_session_user", columnList = "user_id"),
        @Index(name = "idx_auth_session_revoked", columnList = "revokedAt")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class AuthSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 80)
    private String accessTokenId;

    @Column(nullable = false, unique = true, length = 80)
    private String refreshTokenId;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime accessExpiresAt;

    @Column(nullable = false)
    private LocalDateTime refreshExpiresAt;

    private LocalDateTime revokedAt;

    @Column(length = 80)
    private String ipAddress;

    @Column(length = 300)
    private String userAgent;

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return revokedAt == null && refreshExpiresAt.isAfter(now);
    }
}
