package com.hotelerp.userservice.security;

import com.hotelerp.userservice.config.LoginUser;
import com.hotelerp.userservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-token-minutes}") long accessTokenMinutes,
            @Value("${app.security.jwt.refresh-token-days}") long refreshTokenDays) {
        this.signingKey = Keys.hmacShaKeyFor(sha256(secret));
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String generateAccessToken(User user, String tokenId, List<String> authorities) {
        Instant now = Instant.now();
        Long hotelId = null;
        String hotelName = null;
        String licenseStatus = null;
        if (user.getProperty() != null) {
            try {
                hotelId = user.getProperty().getId();
                hotelName = user.getProperty().getName();
                licenseStatus = user.getProperty().getLicenseStatus();
            } catch (EntityNotFoundException ex) {
                // Property entity not found or soft-deleted
            }
        }

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .id(tokenId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(getAccessTokenDuration())))
                .claim("type", "access")
                .claim("userId", user.getId())
                .claim("userName", user.getUsername())
                .claim("hotelId", hotelId)
                .claim("hotelName", hotelName)
                .claim("licenseStatus", licenseStatus)
                .claim("email", user.getEmail())
                .claim("authorities", authorities)
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(User user, String tokenId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .id(tokenId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(getRefreshTokenDuration())))
                .claim("type", "refresh")
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Duration getAccessTokenDuration() {
        return Duration.ofMinutes(accessTokenMinutes);
    }

    public Duration getRefreshTokenDuration() {
        return Duration.ofDays(refreshTokenDays);
    }

    public boolean isAccessToken(Claims claims) {
        return "access".equals(claims.get("type", String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("type", String.class));
    }

    public LoginUser extractLoginUser(String token) {
        Claims claims = parseClaims(token);
        return extractLoginUser(claims);
    }

    @SuppressWarnings("unchecked")
    public LoginUser extractLoginUser(Claims claims) {
        if (claims == null) {
            return null;
        }

        Long userId = extractLongClaim(claims, "userId");
        if (userId == null && claims.getSubject() != null) {
            try {
                userId = Long.parseLong(claims.getSubject());
            } catch (NumberFormatException ignored) {
            }
        }

        String userName = claims.get("userName", String.class);
        String username = claims.get("username", String.class);
        if (userName == null && username != null) {
            userName = username;
        } else if (username == null && userName != null) {
            username = userName;
        }

        Long hotelId = extractLongClaim(claims, "hotelId");
        String hotelName = claims.get("hotelName", String.class);
        String email = claims.get("email", String.class);

        List<String> authorities = null;
        Object rawAuthorities = claims.get("authorities");
        if (rawAuthorities instanceof List<?> list) {
            authorities = list.stream().map(Object::toString).toList();
        }

        return LoginUser.builder()
                .userId(userId)
                .userName(userName)
                .username(username)
                .hotelId(hotelId)
                .hotelName(hotelName)
                .email(email)
                .authorities(authorities)
                .tokenId(claims.getId())
                .build();
    }

    private Long extractLongClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof Number number) {
            return number.longValue();
        } else if (value instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
