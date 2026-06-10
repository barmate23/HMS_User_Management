package com.hotelerp.userservice.controller;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.*;
import com.hotelerp.userservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hmsUserService/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(StandardResponse.success(
                authService.login(request, httpRequest),
                "Login successful"
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<StandardResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(StandardResponse.success(
                authService.refresh(request, httpRequest),
                "Token refreshed successfully"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<StandardResponse<AuthUserResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(StandardResponse.success(
                authService.me(authentication),
                "Current user fetched successfully"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardResponse<Void>> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        authService.logout(authorizationHeader);
        return ResponseEntity.ok(StandardResponse.success("Logout successful"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<StandardResponse<PasswordResetInitResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(StandardResponse.success(
                authService.forgotPassword(request),
                "Password reset code generated"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<StandardResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(StandardResponse.success("Password reset successful"));
    }
}
