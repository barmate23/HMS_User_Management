package com.hotelerp.userservice.config;

import com.hotelerp.userservice.entity.Hotel;
import com.hotelerp.userservice.entity.HotelLicense;
import com.hotelerp.userservice.entity.User;
import com.hotelerp.userservice.repository.HotelLicenseRepository;
import com.hotelerp.userservice.repository.HotelRepository;
import com.hotelerp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DefaultHotelAndAdminInitializer implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final HotelLicenseRepository hotelLicenseRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final Long DEFAULT_HOTEL_ID = 1L;
    private static final String DEFAULT_HOTEL_NAME = "HMS Cloud - Main Hotel";
    private static final String DEFAULT_HOTEL_EMAIL = "admin@hmscloud.com";
    private static final String DEFAULT_LICENSE_KEY = "HMS-LIC-MAIN-0001";

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Ensuring default hotel and admin user linkage...");
        Hotel defaultHotel = ensureDefaultHotel();
        ensureDefaultLicense(defaultHotel);
        attachDefaultHotelToAdmin(defaultHotel);
    }

    private Hotel ensureDefaultHotel() {
        // 1. Try to find existing hotel by ID 1
        Optional<Hotel> hotelOpt = hotelRepository.findById(DEFAULT_HOTEL_ID);
        if (hotelOpt.isPresent()) {
            return hotelOpt.get();
        }

        // 2. Try direct SQL insert with ID 1 to preserve exact ID 1 for client routing
        try {
            jdbcTemplate.execute("""
                INSERT INTO hotels (id, name, email, phone, address, city, state, country, zip_code, total_rooms, currency, is_active, is_deleted, created_on, modified_on, license_key, license_expires_at)
                VALUES (1, 'HMS Cloud - Main Hotel', 'admin@hmscloud.com', '9999999999', '100 Hospitality Way', 'Metropolis', 'Central', 'United States', '10001', 100, 'USD', 1, 0, NOW(), NOW(), 'HMS-LIC-MAIN-0001', DATE_ADD(NOW(), INTERVAL 10 YEAR))
                ON DUPLICATE KEY UPDATE id = id
            """);
            Optional<Hotel> inserted = hotelRepository.findById(DEFAULT_HOTEL_ID);
            if (inserted.isPresent()) {
                log.info("Successfully seeded default hotel with ID: 1 via SQL");
                return inserted.get();
            }
        } catch (Exception e) {
            log.debug("Direct SQL insert for hotel ID 1 skipped or failed: {}", e.getMessage());
        }

        // 3. Fallback: find by email or save via JPA
        return hotelRepository.findByEmail(DEFAULT_HOTEL_EMAIL)
                .orElseGet(() -> {
                    Hotel hotel = Hotel.builder()
                            .name(DEFAULT_HOTEL_NAME)
                            .email(DEFAULT_HOTEL_EMAIL)
                            .phone("9999999999")
                            .address("100 Hospitality Way")
                            .city("Metropolis")
                            .state("Central")
                            .country("United States")
                            .zipCode("10001")
                            .totalRooms(100)
                            .currency("USD")
                            .licenseKey(DEFAULT_LICENSE_KEY)
                            .licenseExpiresAt(LocalDateTime.now().plusYears(10))
                            .isActive(true)
                            .build();
                    Hotel saved = hotelRepository.save(hotel);
                    log.info("Seeded default hotel via JPA with ID: {}", saved.getId());
                    return saved;
                });
    }

    private void ensureDefaultLicense(Hotel hotel) {
        if (hotelLicenseRepository.findByLicenseKey(DEFAULT_LICENSE_KEY).isPresent()
                || (hotel.getId() != null && hotelLicenseRepository.findByHotelId(hotel.getId()).isPresent())) {
            log.debug("Active license for hotel '{}' already exists.", hotel.getName());
            return;
        }

        try {
            HotelLicense license = HotelLicense.builder()
                    .licenseKey(DEFAULT_LICENSE_KEY)
                    .hotel(hotel)
                    .clientEmail(hotel.getEmail())
                    .tier("ENTERPRISE")
                    .maxRooms(hotel.getTotalRooms() != null ? hotel.getTotalRooms() : 100)
                    .maxUsers(50)
                    .enabledModules("Dashboard,Reservations,Arrivals & Departures,Guest Profiles,Housekeeping Board,Room Audit SOP,Lost & Found,Reports,User Management,POS,Inventory,Laundry")
                    .status("ACTIVE")
                    .issuedAt(LocalDateTime.now())
                    .activatedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusYears(10))
                    .portalUrl("http://localhost:5173")
                    .renewalCount(0)
                    .build();

            hotelLicenseRepository.save(license);
            log.info("Seeded default active enterprise license '{}' for hotel ID: {}", DEFAULT_LICENSE_KEY, hotel.getId());
        } catch (Exception e) {
            log.warn("Could not seed default hotel license: {}", e.getMessage());
        }
    }

    private void attachDefaultHotelToAdmin(Hotel hotel) {
        // Link to admin user
        Optional<User> adminOpt = userRepository.findByUsername("admin")
                .or(() -> userRepository.findByEmail(DEFAULT_HOTEL_EMAIL))
                .or(() -> userRepository.findByEmployeeId("EMP-ADMIN"));

        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();
            if (admin.getProperty() == null || !hotel.getId().equals(admin.getProperty().getId())) {
                admin.setProperty(hotel);
                userRepository.save(admin);
                log.info("Linked default admin user '{}' to hotel '{}' (ID: {})",
                        admin.getUsername(), hotel.getName(), hotel.getId());
            }
        }

        // Also ensure via SQL in case any user with username 'admin' has null property_id
        try {
            jdbcTemplate.execute("UPDATE users SET property_id = " + hotel.getId() + " WHERE username = 'admin' AND (property_id IS NULL OR property_id = 0)");
        } catch (Exception ignored) {
        }
    }
}
