package com.hotelerp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long expiresInSeconds;
    private LocalDateTime refreshExpiresAt;
    private AuthUserResponse user;
    private Boolean mustChangePassword;
    private Boolean firstLogin;
    private Boolean passwordChangeRequired;
}
