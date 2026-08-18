package com.hotelerp.userservice.controller;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.constants.ServiceConstants;
import com.hotelerp.userservice.dto.license.*;
import com.hotelerp.userservice.service.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ServiceConstants.LICENSE_BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Licensing & Onboarding Controller", description = "Endpoints for Client Hotel Onboarding, License Generation, Activation, Renewal, and Status Validation")
public class LicenseController {

    private final LicenseService licenseService;

    /**
     * POST /api/hmsUserService/v1/licenses/onboard-client
     * External / Super Admin Endpoint called after receiving client payment confirmation.
     * Automatically creates default Hotel, Admin User credentials, License Key, and sends welcome email.
     */
    @PostMapping(ServiceConstants.ONBOARD_CLIENT)
    @Operation(summary = "Onboard a new Client Hotel", description = "Generates Hotel, default Admin account, License Key, and emails client login & activation details.")
    public ResponseEntity<StandardResponse<ClientOnboardingResponseDto>> onboardClient(
            @Valid @RequestBody ClientOnboardingRequestDto request) {
        ClientOnboardingResponseDto response = licenseService.onboardClient(request);
        return ResponseEntity.ok(StandardResponse.success(
                response,
                "Client hotel onboarded successfully. License details and login credentials sent to client email."
        ));
    }

    /**
     * POST /api/hmsUserService/v1/licenses/activate
     * Endpoint to activate a client HMS portal using the issued License Key.
     */
    @PostMapping(ServiceConstants.ACTIVATE_LICENSE)
    @Operation(summary = "Activate HMS Portal License Key", description = "Validates and activates a license key for an onboarded hotel instance.")
    public ResponseEntity<StandardResponse<LicenseStatusResponseDto>> activateLicense(
            @Valid @RequestBody ActivateLicenseRequestDto request) {
        LicenseStatusResponseDto response = licenseService.activateLicense(request);
        return ResponseEntity.ok(StandardResponse.success(
                response,
                "HMS License Key activated successfully."
        ));
    }

    /**
     * POST /api/hmsUserService/v1/licenses/renew
     * Endpoint to renew/extend subscription license for an existing client hotel upon payment.
     */
    @PostMapping(ServiceConstants.RENEW_LICENSE)
    @Operation(summary = "Renew Hotel Subscription License Key", description = "Generates a new renewal license key with extended expiration date and emails client.")
    public ResponseEntity<StandardResponse<LicenseStatusResponseDto>> renewLicense(
            @Valid @RequestBody RenewLicenseRequestDto request) {
        LicenseStatusResponseDto response = licenseService.renewLicense(request);
        return ResponseEntity.ok(StandardResponse.success(
                response,
                "HMS Subscription license renewed successfully. Renewal license key emailed to client."
        ));
    }

    /**
     * GET /api/hmsUserService/v1/licenses/status/{hotelId}
     * Retrieves current license status, entitlements, and remaining validity for a hotel.
     */
    @GetMapping(ServiceConstants.GET_LICENSE_STATUS)
    @Operation(summary = "Get License Status & Entitlements by Hotel ID", description = "Fetches current license status, active modules, room limits, and expiry date.")
    public ResponseEntity<StandardResponse<LicenseStatusResponseDto>> getLicenseStatus(@PathVariable Long hotelId) {
        LicenseStatusResponseDto response = licenseService.getLicenseStatus(hotelId);
        return ResponseEntity.ok(StandardResponse.success(
                response,
                "License status retrieved successfully."
        ));
    }

    /**
     * GET /api/hmsUserService/v1/licenses/validate?key=...
     * Validates a license key string.
     */
    @GetMapping(ServiceConstants.VALIDATE_LICENSE_KEY)
    @Operation(summary = "Validate License Key string", description = "Checks validity, expiration date, and entitlements of a given license key.")
    public ResponseEntity<StandardResponse<LicenseStatusResponseDto>> validateLicenseKey(@RequestParam("key") String key) {
        LicenseStatusResponseDto response = licenseService.validateLicenseKey(key);
        return ResponseEntity.ok(StandardResponse.success(
                response,
                "License key validated successfully."
        ));
    }
}
