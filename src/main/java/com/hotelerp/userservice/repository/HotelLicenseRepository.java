package com.hotelerp.userservice.repository;

import com.hotelerp.userservice.entity.HotelLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelLicenseRepository extends JpaRepository<HotelLicense, Long> {

    Optional<HotelLicense> findByLicenseKey(String licenseKey);

    Optional<HotelLicense> findByHotelId(Long hotelId);

    List<HotelLicense> findAllByHotelIdOrderByIssuedAtDesc(Long hotelId);

    Optional<HotelLicense> findFirstByHotelIdAndStatusOrderByExpiresAtDesc(Long hotelId, String status);

    List<HotelLicense> findByClientEmail(String clientEmail);

    boolean existsByLicenseKey(String licenseKey);
}
