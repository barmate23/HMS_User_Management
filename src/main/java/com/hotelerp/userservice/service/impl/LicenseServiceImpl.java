package com.hotelerp.userservice.service.impl;

import com.hotelerp.userservice.dto.license.*;
import com.hotelerp.userservice.entity.*;
import com.hotelerp.userservice.entity.Module;
import com.hotelerp.userservice.exception.ResourceNotFoundException;
import com.hotelerp.userservice.repository.*;
import com.hotelerp.userservice.service.LicenseEmailService;
import com.hotelerp.userservice.service.LicenseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LicenseServiceImpl implements LicenseService {

    private final HotelRepository hotelRepository;
    private final HotelLicenseRepository hotelLicenseRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final ShiftRepository shiftRepository;
    private final ModuleRepository moduleRepository;
    private final LicenseEmailService licenseEmailService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.portal.default-url:https://hms.cloud.app}")
    private String defaultPortalUrl;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    @Transactional
    public ClientOnboardingResponseDto onboardClient(ClientOnboardingRequestDto request) {
        log.info("Processing client onboarding request for hotel: {}", request.getHotelName());

        if (hotelRepository.existsByName(request.getHotelName())) {
            throw new IllegalArgumentException("A hotel with name '" + request.getHotelName() + "' already exists.");
        }
        if (hotelRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("A hotel with contact email '" + request.getEmail() + "' already exists.");
        }

        // 1. Create Hotel Entity
        Hotel hotel = Hotel.builder()
                .name(request.getHotelName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress() != null ? request.getAddress() : "127 Main Street")
                .city(request.getCity() != null ? request.getCity() : "New York")
                .state(request.getState() != null ? request.getState() : "NY")
                .country(request.getCountry() != null ? request.getCountry() : "USA")
                .zipCode(request.getZipCode() != null ? request.getZipCode() : "10001")
                .totalRooms(request.getTotalRooms())
                .currency("USD")
                .isActive(true)
                .licenseStatus("PENDING_ACTIVATION")
                .build();

        hotel = hotelRepository.save(hotel);



        Role adminRole = roleRepository.findByName("Hotel Admin")
                .or(() -> roleRepository.findByName("System Administrator"))
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("Hotel Admin")
                        .accessLevel("ADMIN")
                        .status("ACTIVE")
                        .description("Administrator with full hotel management access")
                        .build()));

        Shift defaultShift = shiftRepository.findByShiftCode("MORN")
                .orElseGet(() -> shiftRepository.save(Shift.builder()
                        .shiftName("Morning Shift")
                        .shiftCode("MORN")
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(18, 0))
                        .status("ACTIVE")
                        .build()));

        String tempPassword = StringUtils.hasText(request.getCustomAdminPassword())
                ? request.getCustomAdminPassword()
                : generateSecureTempPassword();

        String employeeId = "EMP-ADMIN-" + String.format("%04d", hotel.getId());

        User adminUser = User.builder()
                .employeeId(employeeId)
                .fullName(request.getAdminFullName())
                .username(request.getAdminUsername())
                .email(StringUtils.hasText(request.getAdminEmail()) ? request.getAdminEmail() : request.getEmail())
                .phone(request.getAdminPhone() != null ? request.getAdminPhone() : request.getPhone())
                .role(adminRole)
                .property(hotel)
                .shift(defaultShift)
                .status("ACTIVE")
                .floorAccess("All Floors")
                .notes("Default Admin User created during hotel client onboarding.")
                .passwordHash(passwordEncoder.encode(tempPassword))
                .mustChangePassword(true)
                .defaultPasswordGeneratedAt(LocalDateTime.now())
                .build();

        adminUser = userRepository.save(adminUser);

        // 3. Generate License Key & Entitlements
        String licenseKey = generateUniqueLicenseKey();
        int validityMonths = request.getValidityMonths() != null && request.getValidityMonths() > 0 ? request.getValidityMonths() : 12;
        LocalDateTime expiresAt = LocalDateTime.now().plusMonths(validityMonths);
        String portalUrl = StringUtils.hasText(request.getPortalUrl()) ? request.getPortalUrl() : defaultPortalUrl;
        String tier = StringUtils.hasText(request.getTier()) ? request.getTier().toUpperCase() : "PROFESSIONAL";
        String enabledModules = determineModulesForTier(tier);
        int maxUsers = calculateUserQuotaForRooms(request.getTotalRooms(), tier);

        HotelLicense license = HotelLicense.builder()
                .licenseKey(licenseKey)
                .hotel(hotel)
                .clientEmail(request.getEmail())
                .tier(tier)
                .maxRooms(request.getTotalRooms())
                .maxUsers(maxUsers)
                .enabledModules(enabledModules)
                .status("PENDING_ACTIVATION")
                .issuedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .portalUrl(portalUrl)
                .renewalCount(0)
                .build();

        hotelLicenseRepository.save(license);

        // Update hotel license references
        hotel.setLicenseKey(licenseKey);
        hotel.setLicenseExpiresAt(expiresAt);
        hotelRepository.save(hotel);

        // 4. Send Onboarding Email
        boolean emailSent = licenseEmailService.sendOnboardingLicenseEmail(
                request.getEmail(),
                hotel.getName(),
                adminUser.getUsername(),
                tempPassword,
                licenseKey,
                portalUrl,
                expiresAt,
                tier,
                request.getTotalRooms()
        );

        log.info("Client onboarding complete for hotel '{}'. LicenseKey: '{}'. Email sent: {}", hotel.getName(), licenseKey, emailSent);

        return ClientOnboardingResponseDto.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelEmail(hotel.getEmail())
                .adminUserId(adminUser.getId())
                .adminFullName(adminUser.getFullName())
                .adminUsername(adminUser.getUsername())
                .adminEmail(adminUser.getEmail())
                .temporaryPassword(tempPassword)
                .licenseKey(licenseKey)
                .licenseStatus("PENDING_ACTIVATION")
                .tier(tier)
                .maxRooms(request.getTotalRooms())
                .maxUsers(maxUsers)
                .enabledModules(enabledModules)
                .issuedAt(license.getIssuedAt())
                .expiresAt(expiresAt)
                .portalUrl(portalUrl)
                .emailSent(emailSent)
                .message("Hotel onboarding successful. License key and default credentials issued to client via email.")
                .build();
    }

    @Override
    @Transactional
    public LicenseStatusResponseDto activateLicense(ActivateLicenseRequestDto request) {
        log.info("Activating license key: {}", request.getLicenseKey());

        HotelLicense license = hotelLicenseRepository.findByLicenseKey(request.getLicenseKey())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or non-existent license key: " + request.getLicenseKey()));

        if ("REVOKED".equalsIgnoreCase(license.getStatus())) {
            throw new IllegalStateException("License key has been revoked and cannot be activated.");
        }

        if (LocalDateTime.now().isAfter(license.getExpiresAt())) {
            license.setStatus("EXPIRED");
            hotelLicenseRepository.save(license);
            throw new IllegalStateException("License key has expired on " + license.getExpiresAt() + ". Please request a renewal key.");
        }

        license.setStatus("ACTIVE");
        license.setActivatedAt(LocalDateTime.now());
        hotelLicenseRepository.save(license);

        Hotel hotel = license.getHotel();
        hotel.setIsActive(true);
        hotel.setLicenseStatus("ACTIVE");
        hotel.setLicenseKey(license.getLicenseKey());
        hotel.setLicenseExpiresAt(license.getExpiresAt());
        hotelRepository.save(hotel);

        log.info("License key '{}' activated successfully for hotel '{}'.", request.getLicenseKey(), hotel.getName());

        return buildLicenseStatusResponse(license, "License activated successfully. Welcome to HMS Cloud!");
    }

    @Override
    @Transactional
    public LicenseStatusResponseDto renewLicense(RenewLicenseRequestDto request) {
        log.info("Processing license renewal for hotelId: {}", request.getHotelId());

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + request.getHotelId()));

        int validityMonths = request.getValidityMonths() != null && request.getValidityMonths() > 0 ? request.getValidityMonths() : 12;

        String newTier = StringUtils.hasText(request.getNewTier()) ? request.getNewTier().toUpperCase() : "PROFESSIONAL";
        int newMaxRooms = request.getNewMaxRooms() != null && request.getNewMaxRooms() > 0 ? request.getNewMaxRooms() : hotel.getTotalRooms();
        String enabledModules = determineModulesForTier(newTier);
        int maxUsers = request.getNewMaxUsers() != null ? request.getNewMaxUsers() : calculateUserQuotaForRooms(newMaxRooms, newTier);

        String newLicenseKey = generateUniqueLicenseKey();
        LocalDateTime newExpiresAt = LocalDateTime.now().plusMonths(validityMonths);

        HotelLicense newLicense = HotelLicense.builder()
                .licenseKey(newLicenseKey)
                .hotel(hotel)
                .clientEmail(hotel.getEmail())
                .tier(newTier)
                .maxRooms(newMaxRooms)
                .maxUsers(maxUsers)
                .enabledModules(enabledModules)
                .status("ACTIVE")
                .issuedAt(LocalDateTime.now())
                .activatedAt(LocalDateTime.now())
                .expiresAt(newExpiresAt)
                .portalUrl(defaultPortalUrl)
                .renewalCount(1)
                .lastRenewalAt(LocalDateTime.now())
                .build();

        hotelLicenseRepository.save(newLicense);

        // Update Hotel references
        hotel.setTotalRooms(newMaxRooms);
        hotel.setLicenseKey(newLicenseKey);
        hotel.setLicenseStatus("ACTIVE");
        hotel.setLicenseExpiresAt(newExpiresAt);
        hotel.setIsActive(true);
        hotelRepository.save(hotel);

        // Email client with new License Key
        boolean emailSent = licenseEmailService.sendLicenseRenewalEmail(
                hotel.getEmail(),
                hotel.getName(),
                newLicenseKey,
                newExpiresAt,
                defaultPortalUrl,
                newTier,
                newMaxRooms
        );

        log.info("License renewed for hotel '{}'. New LicenseKey: '{}'. Email sent: {}", hotel.getName(), newLicenseKey, emailSent);

        return buildLicenseStatusResponse(newLicense, "License renewed successfully. New license key issued and emailed to client.");
    }

    @Override
    public LicenseStatusResponseDto getLicenseStatus(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        Optional<HotelLicense> licenseOpt = hotelLicenseRepository.findFirstByHotelIdAndStatusOrderByExpiresAtDesc(hotelId, "ACTIVE")
                .or(() -> hotelLicenseRepository.findFirstByHotelIdAndStatusOrderByExpiresAtDesc(hotelId, "PENDING_ACTIVATION"))
                .or(() -> hotelLicenseRepository.findByLicenseKey(hotel.getLicenseKey()));

        if (licenseOpt.isEmpty()) {
            return LicenseStatusResponseDto.builder()
                    .hotelId(hotel.getId())
                    .hotelName(hotel.getName())
                    .clientEmail(hotel.getEmail())
                    .status("INACTIVE")
                    .isActive(false)
                    .message("No license found for this hotel.")
                    .build();
        }

        HotelLicense license = licenseOpt.get();
        return buildLicenseStatusResponse(license, "License status retrieved.");
    }

    @Override
    public LicenseStatusResponseDto validateLicenseKey(String licenseKey) {
        HotelLicense license = hotelLicenseRepository.findByLicenseKey(licenseKey)
                .orElseThrow(() -> new ResourceNotFoundException("License key not found: " + licenseKey));

        return buildLicenseStatusResponse(license, "License key validation result.");
    }

    private LicenseStatusResponseDto buildLicenseStatusResponse(HotelLicense license, String message) {
        LocalDateTime now = LocalDateTime.now();
        boolean isExpired = now.isAfter(license.getExpiresAt());
        String currentStatus = isExpired ? "EXPIRED" : license.getStatus();
        boolean isActive = "ACTIVE".equalsIgnoreCase(currentStatus) && !isExpired;

        long daysRemaining = isExpired ? 0 : ChronoUnit.DAYS.between(now, license.getExpiresAt());

        List<String> modules = StringUtils.hasText(license.getEnabledModules())
                ? Arrays.stream(license.getEnabledModules().split(","))
                .map(String::trim)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return LicenseStatusResponseDto.builder()
                .hotelId(license.getHotel().getId())
                .hotelName(license.getHotel().getName())
                .clientEmail(license.getClientEmail())
                .licenseKey(license.getLicenseKey())
                .status(currentStatus)
                .isActive(isActive)
                .tier(license.getTier())
                .maxRooms(license.getMaxRooms())
                .maxUsers(license.getMaxUsers())
                .enabledModules(modules)
                .issuedAt(license.getIssuedAt())
                .activatedAt(license.getActivatedAt())
                .expiresAt(license.getExpiresAt())
                .daysRemaining(daysRemaining)
                .message(message)
                .build();
    }

    private String generateUniqueLicenseKey() {
        String key;
        do {
            key = "HMS-LIC-" + randomSegment(4) + "-" + randomSegment(4) + "-" + randomSegment(4);
        } while (hotelLicenseRepository.existsByLicenseKey(key));
        return key;
    }

    private String randomSegment(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    private String generateSecureTempPassword() {
        return "HmsAdmin#" + (1000 + RANDOM.nextInt(9000));
    }

    private String determineModulesForTier(String tier) {
        if ("STARTER".equalsIgnoreCase(tier)) {
            return "RESERVATIONS,ARRIVALS_DEPARTURES,GUEST_PROFILES,REPORTS";
        } else if ("ENTERPRISE".equalsIgnoreCase(tier)) {
            return "RESERVATIONS,ARRIVALS_DEPARTURES,GUEST_PROFILES,HOUSEKEEPING,BILLING,LAUNDRY,INVENTORY,PURCHASE,CRM,POS,KDS,CHANNEL_MANAGER,REPORTS,USER_MANAGEMENT";
        } else {
            // PROFESSIONAL (default)
            return "RESERVATIONS,ARRIVALS_DEPARTURES,GUEST_PROFILES,HOUSEKEEPING,BILLING,LAUNDRY,REPORTS,POS";
        }
    }

    private int calculateUserQuotaForRooms(int totalRooms, String tier) {
        if ("ENTERPRISE".equalsIgnoreCase(tier)) return 100;
        if ("STARTER".equalsIgnoreCase(tier)) return Math.max(5, totalRooms / 5);
        return Math.max(15, totalRooms / 2); // Professional default
    }
}
