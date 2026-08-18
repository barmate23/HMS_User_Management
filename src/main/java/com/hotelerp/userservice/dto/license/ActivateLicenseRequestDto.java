package com.hotelerp.userservice.dto.license;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivateLicenseRequestDto {

    private Long hotelId;

    @NotBlank(message = "License key is required")
    private String licenseKey;
}
