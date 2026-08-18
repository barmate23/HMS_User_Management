package com.hotelerp.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotels", indexes = {
        @Index(name = "idx_city", columnList = "city"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_active", columnList = "isActive")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = false")
public class Hotel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "zipCode", nullable = false, length = 10)
    private String zipCode;

    @Column(name = "totalRooms", nullable = false)
    private Integer totalRooms;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "licenseKey", length = 100)
    private String licenseKey;

    @Builder.Default
    @Column(name = "licenseStatus", length = 30)
    private String licenseStatus = "PENDING_ACTIVATION";

    @Column(name = "licenseExpiresAt")
    private LocalDateTime licenseExpiresAt;

    @Builder.Default
    @Column(name = "isActive")
    private Boolean isActive = true;

    @PrePersist
    protected void initFields() {
        if (isActive == null) isActive = true;
        if (currency == null) currency = "USD";
        if (licenseStatus == null) licenseStatus = "PENDING_ACTIVATION";
    }
}