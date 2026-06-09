package com.hotelerp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserResponse {
    private Long id;
    private String employeeId;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String roleCode;
    private String department;
    private String property;
    private String status;
    private LocalDateTime lastLoginAt;
    private List<String> authorities;
}
