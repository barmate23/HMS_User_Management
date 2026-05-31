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

    /**
     * Search users by name, username, email or employee-id.
     * Supports optional department and role filters.
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:searchText IS NULL OR " +
            "  u.fullName   LIKE %:searchText% OR " +
            "  u.username   LIKE %:searchText% OR " +
            "  u.email      LIKE %:searchText% OR " +
            "  u.employeeId LIKE %:searchText%) AND " +
            "(:department IS NULL OR u.department = :department) AND " +
            "(:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(
            @Param("searchText")  String searchText,
            @Param("department")  String department,
            @Param("role")        String role,
            Pageable pageable
    );

    long countByRole(String role);
}
