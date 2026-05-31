package com.hotelerp.userservice.controller;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.constants.ServiceConstants;
import com.hotelerp.userservice.dto.UserRequest;
import com.hotelerp.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ServiceConstants.USER_BASE_URL)
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    /** POST /api/v1/users/createUser */
    @PostMapping(ServiceConstants.CREATE_USER)
    public ResponseEntity<StandardResponse<?>> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(service.createUser(request));
    }

    /** PUT /api/v1/users/updateUser/{id} */
    @PutMapping(ServiceConstants.UPDATE_USER)
    public ResponseEntity<StandardResponse<?>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(service.updateUser(id, request));
    }

    /** GET /api/v1/users/getUserById/{id} */
    @GetMapping(ServiceConstants.GET_USER_BY_ID)
    public ResponseEntity<StandardResponse<?>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUserById(id));
    }

    /**
     * GET /api/v1/users/getAllUsers
     * Query params: searchText, department, role, page (0-based), size
     * Matches the User Directory listing with filters shown in the UI.
     */
    @GetMapping(ServiceConstants.GET_ALL_USERS)
    public ResponseEntity<StandardResponse<?>> getAllUsers(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAllUsers(searchText, department, role, page, size));
    }

    /** DELETE /api/v1/users/deleteUser/{id}  – soft delete (marks INACTIVE) */
    @DeleteMapping(ServiceConstants.DELETE_USER)
    public ResponseEntity<StandardResponse<?>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(service.deleteUser(id));
    }

    /**
     * PATCH /api/v1/users/changeStatus/{id}?status=LOCKED
     * Supports the lock/unlock action visible in the listing screen.
     */
    @PatchMapping(ServiceConstants.CHANGE_STATUS)
    public ResponseEntity<StandardResponse<?>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(service.changeStatus(id, status));
    }
}
