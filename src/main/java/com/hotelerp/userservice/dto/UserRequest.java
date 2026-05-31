package com.hotelerp.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    private String department;

    @NotBlank(message = "Role is required")
    private String role;

    private String property;

    private String shift;

    /** ACTIVE | LOCKED | INACTIVE  (defaults to ACTIVE) */
    private String status;

    /**
     * Floor access list – e.g. ["Floor 1", "All Floors"]
     * Will be joined as comma-separated string for storage.
     */
    private List<String> floorAccess;

    private String notes;

    /** Plain-text password – will be hashed before persisting */
    private String password;
}
