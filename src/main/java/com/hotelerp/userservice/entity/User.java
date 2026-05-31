package com.hotelerp.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_username",    columnList = "username",   unique = true),
        @Index(name = "idx_email",       columnList = "email",      unique = true),
        @Index(name = "idx_employee_id", columnList = "employeeId", unique = true),
        @Index(name = "idx_department",  columnList = "department"),
        @Index(name = "idx_status",      columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. EMP-050 */
    @Column(name = "employeeId", nullable = false, unique = true, length = 20)
    private String employeeId;

    @Column(name = "fullName", nullable = false, length = 150)
    private String fullName;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    /** e.g. Front Office, Housekeeping, Accounts, Management */
    @Column(name = "department", length = 100)
    private String department;

    /** e.g. Property Administrator, Front Office Manager, Room Attendant */
    @Column(name = "role", nullable = false, length = 100)
    private String role;

    /** e.g. HMS Cloud - Main Hotel */
    @Column(name = "property", length = 200)
    private String property;

    /** e.g. Morning Shift, Evening Shift, Night Shift */
    @Column(name = "shift", length = 50)
    private String shift;

    /** ACTIVE | LOCKED | INACTIVE */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** Comma-separated list of floors, e.g. "Floor 1,Floor 2" or "All Floors" */
    @Column(name = "floorAccess", columnDefinition = "TEXT")
    private String floorAccess;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Hashed password – stored but never returned in responses */
    @Column(name = "passwordHash", length = 255)
    private String passwordHash;

    @Column(name = "lastLoginAt")
    private LocalDateTime lastLoginAt;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
