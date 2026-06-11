package com.hotelerp.userservice.service;

import com.hotelerp.userservice.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);
    AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest);
    AuthUserResponse me(Authentication authentication);
    void logout(String authorizationHeader);
    PasswordResetInitResponse forgotPassword(ForgotPasswordRequest request);
    void verifyResetCode(VerifyResetCodeRequest request);
    void resetPassword(ResetPasswordRequest request);
    void changePassword(ChangePasswordRequest request);
}
