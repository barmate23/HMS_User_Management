package com.hotelerp.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyResetCodeRequest {
    @Email(message = "Enter a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Reset code is required")
    @Pattern(regexp = "\\d{6}", message = "Reset code must be 6 digits")
    private String resetCode;
}
