package com.hotelerp.userservice.service;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.CommonMasterResponse;
import com.hotelerp.userservice.dto.UserRequest;
import com.hotelerp.userservice.dto.UserResponse;
import com.hotelerp.userservice.entity.CommonMaster;
import com.hotelerp.userservice.entity.User;
import com.hotelerp.userservice.repository.CommonMasterRepository;
import com.hotelerp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final CommonMasterRepository commonMasterRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StandardResponse<?> createUser(UserRequest request) {
        log.info("Request received to create user: {}", request.getUsername());
        try {
            if (repository.existsByUsername(request.getUsername())) {
                return StandardResponse.error("Username already exists", "DUPLICATE_USERNAME",
                        "username", request.getUsername());
            }
            if (repository.existsByEmail(request.getEmail())) {
                return StandardResponse.error("Email already registered", "DUPLICATE_EMAIL",
                        "email", request.getEmail());
            }
            if (repository.existsByEmployeeId(request.getEmployeeId())) {
                return StandardResponse.error("Employee ID already exists", "DUPLICATE_EMPLOYEE_ID",
                        "employeeId", request.getEmployeeId());
            }

            User user = User.builder()
                    .employeeId(request.getEmployeeId())
                    .fullName(request.getFullName())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .department(request.getDepartmentId() != null 
                            ? commonMasterRepository.findById(request.getDepartmentId()).orElse(null) : null)
                    .role(request.getRoleId() != null 
                            ? commonMasterRepository.findById(request.getRoleId()).orElse(null) : null)
                    .property(request.getPropertyId() != null 
                            ? commonMasterRepository.findById(request.getPropertyId()).orElse(null) : null)
                    .shift(request.getShift())
                    .status(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ACTIVE")
                    .floorAccess(joinFloors(request.getFloorAccess()))
                    .notes(request.getNotes())
                    .passwordHash(request.getPassword() != null
                            ? passwordEncoder.encode(request.getPassword()) : null)
                    .build();

            User saved = repository.save(user);
            log.info("User created with ID: {}", saved.getId());
            return StandardResponse.success(mapToResponse(saved), "User created successfully");
        } catch (Exception e) {
            log.error("Error creating user: ", e);
            return StandardResponse.error("Failed to create user", "CREATE_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StandardResponse<?> updateUser(Long id, UserRequest request) {
        log.info("Request received to update user ID: {}", id);
        try {
            Optional<User> existingOpt = repository.findById(id);
            if (existingOpt.isEmpty()) {
                return StandardResponse.error("User not found", "NOT_FOUND", "id", null);
            }
            User user = existingOpt.get();

            // Check duplicates only when value changed
            if (!user.getUsername().equals(request.getUsername())
                    && repository.existsByUsername(request.getUsername())) {
                return StandardResponse.error("Username already exists", "DUPLICATE_USERNAME",
                        "username", request.getUsername());
            }
            if (!user.getEmail().equals(request.getEmail())
                    && repository.existsByEmail(request.getEmail())) {
                return StandardResponse.error("Email already registered", "DUPLICATE_EMAIL",
                        "email", request.getEmail());
            }
            if (!user.getEmployeeId().equals(request.getEmployeeId())
                    && repository.existsByEmployeeId(request.getEmployeeId())) {
                return StandardResponse.error("Employee ID already exists", "DUPLICATE_EMPLOYEE_ID",
                        "employeeId", request.getEmployeeId());
            }

            user.setEmployeeId(request.getEmployeeId());
            user.setFullName(request.getFullName());
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            
            if (request.getDepartmentId() != null) {
                user.setDepartment(commonMasterRepository.findById(request.getDepartmentId()).orElse(null));
            }
            if (request.getRoleId() != null) {
                user.setRole(commonMasterRepository.findById(request.getRoleId()).orElse(null));
            }
            if (request.getPropertyId() != null) {
                user.setProperty(commonMasterRepository.findById(request.getPropertyId()).orElse(null));
            }

            user.setShift(request.getShift());
            if (StringUtils.hasText(request.getStatus())) {
                user.setStatus(request.getStatus());
            }
            user.setFloorAccess(joinFloors(request.getFloorAccess()));
            user.setNotes(request.getNotes());

            if (StringUtils.hasText(request.getPassword())) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            User updated = repository.save(user);
            return StandardResponse.success(mapToResponse(updated), "User updated successfully");
        } catch (Exception e) {
            log.error("Error updating user: ", e);
            return StandardResponse.error("Failed to update user", "UPDATE_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getUserById(Long id) {
        log.info("Fetching user ID: {}", id);
        try {
            return repository.findById(id)
                    .map(user -> StandardResponse.success(mapToResponse(user), "User fetched successfully"))
                    .orElse(StandardResponse.error("User not found", "NOT_FOUND", "id", null));
        } catch (Exception e) {
            log.error("Error fetching user: ", e);
            return StandardResponse.error("Failed to fetch user", "FETCH_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL (paginated + searchable)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public StandardResponse<?> getAllUsers(String searchText, String department, String role, int page, int size) {
        log.info("Fetching users – searchText={}, dept={}, role={}, page={}, size={}", searchText, department,
                role, page, size);
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = repository.searchUsers(
                    StringUtils.hasText(searchText) ? searchText : null,
                    StringUtils.hasText(department) ? department : null,
                    StringUtils.hasText(role) ? role : null,
                    pageable);

            List<UserResponse> responses = userPage.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                    .totalRecords(userPage.getTotalElements())
                    .currentPage(userPage.getNumber())
                    .pageSize(userPage.getSize())
                    .totalPages(userPage.getTotalPages())
                    .build();

            return StandardResponse.success(responses, "Users fetched successfully", metadata);
        } catch (Exception e) {
            log.error("Error fetching users: ", e);
            return StandardResponse.error("Failed to fetch users", "FETCH_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE (soft delete → INACTIVE)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StandardResponse<?> deleteUser(Long id) {
        log.info("Soft-deleting user ID: {}", id);
        try {
            Optional<User> opt = repository.findById(id);
            if (opt.isEmpty()) {
                return StandardResponse.error("User not found", "NOT_FOUND", "id", null);
            }
            User user = opt.get();
            user.setStatus("INACTIVE");
            repository.save(user);
            return StandardResponse.success("User deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting user: ", e);
            return StandardResponse.error("Failed to delete user", "DELETE_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHANGE STATUS  (ACTIVE / LOCKED / INACTIVE)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StandardResponse<?> changeStatus(Long id, String status) {
        log.info("Changing status of user ID: {} to {}", id, status);
        try {
            List<String> validStatuses = List.of("ACTIVE", "LOCKED", "INACTIVE");
            if (!validStatuses.contains(status.toUpperCase())) {
                return StandardResponse.error("Invalid status value", "INVALID_STATUS",
                        "status", "Allowed: ACTIVE, LOCKED, INACTIVE");
            }
            Optional<User> opt = repository.findById(id);
            if (opt.isEmpty()) {
                return StandardResponse.error("User not found", "NOT_FOUND", "id", null);
            }
            User user = opt.get();
            user.setStatus(status.toUpperCase());
            repository.save(user);
            return StandardResponse.success("User status updated to " + status.toUpperCase());
        } catch (Exception e) {
            log.error("Error changing user status: ", e);
            return StandardResponse.error("Failed to change status", "STATUS_ERROR", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Convert List<String> floor list to comma-separated string for DB storage */
    private String joinFloors(List<String> floors) {
        if (floors == null || floors.isEmpty()) return null;
        return String.join(",", floors);
    }

    /** Parse comma-separated floor string back to list */
    private List<String> splitFloors(String floorAccess) {
        if (!StringUtils.hasText(floorAccess)) return Collections.emptyList();
        return Arrays.asList(floorAccess.split(","));
    }

    private CommonMasterResponse mapToCommonMasterResponse(CommonMaster master) {
        if (master == null) return null;
        return CommonMasterResponse.builder()
                .id(master.getId())
                .category(master.getCategory())
                .code(master.getCode())
                .value(master.getValue())
                .build();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .department(mapToCommonMasterResponse(user.getDepartment()))
                .role(mapToCommonMasterResponse(user.getRole()))
                .property(mapToCommonMasterResponse(user.getProperty()))
                .shift(user.getShift())
                .status(user.getStatus())
                .floorAccess(splitFloors(user.getFloorAccess()))
                .notes(user.getNotes())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
