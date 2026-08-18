package com.hotelerp.userservice.dto.license;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenewLicenseRequestDto {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    /** Validity extension in months (e.g. 12) */
    @Builder.Default
    private Integer validityMonths = 12;

    /** Optional tier change (e.g. STARTER, PROFESSIONAL, ENTERPRISE) */
    private String newTier;

    /** Optional room limit update */
    private Integer newMaxRooms;

    /** Optional custom user limit update */
    private Integer newMaxUsers;
}
