package com.hotelerp.userservice.service.impl;

import com.hotelerp.userservice.dto.*;
import com.hotelerp.userservice.entity.AuthSession;
import com.hotelerp.userservice.entity.CommonMaster;
import com.hotelerp.userservice.entity.Department;
import com.hotelerp.userservice.entity.PasswordResetToken;
import com.hotelerp.userservice.entity.Role;
import com.hotelerp.userservice.entity.User;
import com.hotelerp.userservice.repository.AuthSessionRepository;
import com.hotelerp.userservice.repository.PasswordResetTokenRepository;
import com.hotelerp.userservice.repository.UserRepository;
import com.hotelerp.userservice.security.JwtService;
import com.hotelerp.userservice.service.AuthService;
import com.hotelerp.userservice.service.UserCredentialEmailService;
import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserCredentialEmailService credentialEmailService;

    @Value("${app.security.password-reset.token-minutes}")
    private long passwordResetMinutes;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String identifier = normalize(request.getIdentifier());
        User user = userRepository.findByUsernameOrEmailIgnoreCase(identifier)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid username/email or password"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(FORBIDDEN, "User account is not active");
        }
        if (!StringUtils.hasText(user.getPasswordHash()) || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid username/email or password");
        }
        if (Boolean.TRUE.equals(user.getMustChangePassword())) {
            return AuthResponse.builder()
                    .mustChangePassword(true)
                    .firstLogin(true)
                    .passwordChangeRequired(true)
                    .user(toAuthUser(user, buildAuthorities(user)))
                    .build();
        }

        return issueSession(user, httpRequest);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        Claims claims = jwtService.parseClaims(request.getRefreshToken());
        if (!jwtService.isRefreshToken(claims)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid refresh token");
        }

        AuthSession session = authSessionRepository.findByRefreshTokenId(claims.getId())
                .filter(AuthSession::isActive)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Session is invalid or expired"));

        User user = session.getUser();
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus()) || !String.valueOf(user.getId()).equals(claims.getSubject())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Session user is invalid");
        }

        LocalDateTime now = LocalDateTime.now();
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();
        List<String> authorities = buildAuthorities(user);

        session.setAccessTokenId(accessTokenId);
        session.setRefreshTokenId(refreshTokenId);
        session.setIssuedAt(now);
        session.setAccessExpiresAt(now.plus(jwtService.getAccessTokenDuration()));
        session.setRefreshExpiresAt(now.plus(jwtService.getRefreshTokenDuration()));
        session.setIpAddress(resolveClientIp(httpRequest));
        session.setUserAgent(httpRequest.getHeader("User-Agent"));
        authSessionRepository.save(session);

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(jwtService.generateAccessToken(user, accessTokenId, authorities))
                .refreshToken(jwtService.generateRefreshToken(user, refreshTokenId))
                .expiresInSeconds(jwtService.getAccessTokenDuration().toSeconds())
                .refreshExpiresAt(session.getRefreshExpiresAt())
                .user(toAuthUser(user, authorities))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication is required");
        }

        Long userId = Long.valueOf(authentication.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return toAuthUser(user, buildAuthorities(user));
    }

    @Override
    @Transactional
    public void logout(String authorizationHeader) {
        String token = resolveBearerToken(authorizationHeader);
        if (token == null) {
            return;
        }

        Claims claims = jwtService.parseClaims(token);
        authSessionRepository.findByAccessTokenId(claims.getId())
                .ifPresent(session -> {
                    session.setRevokedAt(LocalDateTime.now());
                    authSessionRepository.save(session);
                });
    }

    @Override
    @Transactional
    public PasswordResetInitResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalize(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(passwordResetMinutes);
        String resetCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .email(email)
                .resetCode(resetCode)
                .expiresAt(expiresAt)
                .build());

        boolean emailSent = credentialEmailService.sendPasswordResetOtp(user, resetCode, expiresAt);

        return PasswordResetInitResponse.builder()
                .email(email)
                .expiresAt(expiresAt)
                .resetCode(emailSent ? null : resetCode)
                .deliveryMode(emailSent ? "EMAIL" : "RESPONSE")
                .emailSent(emailSent)
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalize(request.getEmail());
        PasswordResetToken token = passwordResetTokenRepository
                .findTopByEmailAndResetCodeOrderByCreatedAtDesc(email, request.getResetCode())
                .filter(PasswordResetToken::isUsable)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Reset code is invalid or expired"));

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setDefaultPasswordGeneratedAt(null);
        userRepository.save(user);

        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);

        List<AuthSession> activeSessions = authSessionRepository.findByUserIdAndRevokedAtIsNull(user.getId());
        for (AuthSession session : activeSessions) {
            session.setRevokedAt(LocalDateTime.now());
        }
        authSessionRepository.saveAll(activeSessions);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String identifier = normalize(request.getIdentifier());
        String currentPassword = request.effectiveCurrentPassword();
        if (!StringUtils.hasText(currentPassword)) {
            throw new ResponseStatusException(BAD_REQUEST, "Current or temporary password is required");
        }
        if (StringUtils.hasText(request.getConfirmPassword()) && !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "New password and confirmation must match");
        }
        if (request.getNewPassword().equals(currentPassword)) {
            throw new ResponseStatusException(BAD_REQUEST, "New password must be different from the temporary password");
        }

        User user = userRepository.findByUsernameOrEmailIgnoreCase(identifier)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(FORBIDDEN, "User account is not active");
        }
        if (!StringUtils.hasText(user.getPasswordHash()) || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Current password is invalid");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setDefaultPasswordGeneratedAt(null);
        userRepository.save(user);

        List<AuthSession> activeSessions = authSessionRepository.findByUserIdAndRevokedAtIsNull(user.getId());
        for (AuthSession session : activeSessions) {
            session.setRevokedAt(LocalDateTime.now());
        }
        authSessionRepository.saveAll(activeSessions);
    }

    private AuthResponse issueSession(User user, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();
        List<String> authorities = buildAuthorities(user);

        AuthSession session = AuthSession.builder()
                .user(user)
                .accessTokenId(accessTokenId)
                .refreshTokenId(refreshTokenId)
                .issuedAt(now)
                .accessExpiresAt(now.plus(jwtService.getAccessTokenDuration()))
                .refreshExpiresAt(now.plus(jwtService.getRefreshTokenDuration()))
                .ipAddress(resolveClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();
        authSessionRepository.save(session);

        user.setLastLoginAt(now);
        userRepository.save(user);

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(jwtService.generateAccessToken(user, accessTokenId, authorities))
                .refreshToken(jwtService.generateRefreshToken(user, refreshTokenId))
                .expiresInSeconds(jwtService.getAccessTokenDuration().toSeconds())
                .refreshExpiresAt(session.getRefreshExpiresAt())
                .user(toAuthUser(user, authorities))
                .build();
    }

    private AuthUserResponse toAuthUser(User user, List<String> authorities) {
        return AuthUserResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(valueOf(user.getRole()))
                .roleCode(codeOf(user.getRole()))
                .department(valueOf(user.getDepartment()))
                .property(valueOf(user.getProperty()))
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .authorities(authorities)
                .build();
    }

    private List<String> buildAuthorities(User user) {
        List<String> authorities = new ArrayList<>();
        String roleCode = codeOf(user.getRole());
        if (StringUtils.hasText(roleCode)) {
            authorities.add("ROLE_" + roleCode.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_"));
        }
        if (authorities.isEmpty()) {
            authorities.add("ROLE_USER");
        }
        return authorities;
    }

    private String valueOf(CommonMaster item) {
        return item == null ? null : item.getValue();
    }

    private String valueOf(Role role) {
        try {
            return role == null ? null : role.getName();
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "User profile is linked to a role that no longer exists. Please update the user's role before login."
            );
        }
    }

    private String valueOf(Department department) {
        try {
            return department == null ? null : department.getName();
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "User profile is linked to a department that no longer exists. Please update the user's department before login."
            );
        }
    }

    private String codeOf(CommonMaster item) {
        if (item == null) {
            return null;
        }
        return StringUtils.hasText(item.getCode()) ? item.getCode() : item.getValue();
    }

    private String codeOf(Role role) {
        try {
            return role == null ? null : role.getName().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "User profile is linked to a role that no longer exists. Please update the user's role before login."
            );
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
