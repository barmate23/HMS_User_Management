package com.hotelerp.userservice.service;

import com.hotelerp.userservice.dto.license.*;

public interface LicenseService {

    /**
     * External/Super Admin API to onboard a client hotel after payment confirmation.
     * Creates Hotel, Default Admin User, generates License Key, and sends welcome email.
     */
    ClientOnboardingResponseDto onboardClient(ClientOnboardingRequestDto request);

    /**
     * Activates a license key for a client hotel instance.
     */
    LicenseStatusResponseDto activateLicense(ActivateLicenseRequestDto request);

    /**
     * Renews or extends a license for a hotel client upon payment renewal.
     * Generates new license key, updates expiration date, and emails client.
     */
    LicenseStatusResponseDto renewLicense(RenewLicenseRequestDto request);

    /**
     * Gets license status and entitlement details for a given hotel.
     */
    LicenseStatusResponseDto getLicenseStatus(Long hotelId);

    /**
     * Validates a license key string directly.
     */
    LicenseStatusResponseDto validateLicenseKey(String licenseKey);
}
