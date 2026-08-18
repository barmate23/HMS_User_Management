package com.hotelerp.userservice.repository;

import com.hotelerp.userservice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    Optional<Hotel> findByEmail(String email);
    Optional<Hotel> findByLicenseKey(String licenseKey);
}
