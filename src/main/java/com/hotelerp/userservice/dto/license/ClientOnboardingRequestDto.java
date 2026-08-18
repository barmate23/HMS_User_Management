package com.hotelerp.userservice.dto.license;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientOnboardingRequestDto {

    @NotBlank(message = "Hotel name is required")
    private String hotelName;

    @NotBlank(message = "Hotel contact email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @Builder.Default
    private String address = "127 Main Street";

    @Builder.Default
    private String city = "New York";

    @Builder.Default
    private String state = "NY";

    @Builder.Default
    private String country = "USA";

    @Builder.Default
    private String zipCode = "10001";

    @NotNull(message = "Total rooms is required")
    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    /** STARTER | PROFESSIONAL | ENTERPRISE */
    @Builder.Default
    private String tier = "PROFESSIONAL";

    /** Subscription period in months (e.g. 1, 6, 12) */
    @Builder.Default
    private Integer validityMonths = 12;

    private String portalUrl;

    // Admin Details
    @NotBlank(message = "Admin full name is required")
    private String adminFullName;

    @NotBlank(message = "Admin username is required")
    private String adminUsername;

    @Email(message = "Invalid admin email format")
    private String adminEmail;

    private String adminPhone;

    /** Optional custom password. If blank, auto-generates secure temp password */
    private String customAdminPassword;
}
