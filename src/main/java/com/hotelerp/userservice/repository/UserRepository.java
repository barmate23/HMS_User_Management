package com.hotelerp.userservice.repository;

import com.hotelerp.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)")
    Optional<User> findByUsernameOrEmailIgnoreCase(@Param("identifier") String identifier);

    /**
     * Search users by name, username, email or employee-id.
     * Supports optional department, role, and hotelId/property filters.
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN u.department d " +
            "LEFT JOIN u.role r " +
            "LEFT JOIN u.property p " +
            "WHERE " +
            "(:searchText IS NULL OR " +
            "  u.fullName   LIKE %:searchText% OR " +
            "  u.username   LIKE %:searchText% OR " +
            "  u.email      LIKE %:searchText% OR " +
            "  u.employeeId LIKE %:searchText%) AND " +
            "(:department IS NULL OR d.name = :department) AND " +
            "(:role IS NULL OR r.name = :role) AND " +
            "(:hotelId IS NULL OR p.id = :hotelId)")
    Page<User> searchUsers(
            @Param("searchText")  String searchText,
            @Param("department")  String department,
            @Param("role")        String role,
            @Param("hotelId")     Long hotelId,
            Pageable pageable
    );

    long countByRoleName(String roleName);
}
