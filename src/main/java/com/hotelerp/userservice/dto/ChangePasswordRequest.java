package com.hotelerp.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Email or username is required")
    private String identifier;

    private String currentPassword;

    private String temporaryPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;

    private String confirmPassword;

    public String effectiveCurrentPassword() {
        return currentPassword != null && !currentPassword.isBlank() ? currentPassword : temporaryPassword;
    }
}
