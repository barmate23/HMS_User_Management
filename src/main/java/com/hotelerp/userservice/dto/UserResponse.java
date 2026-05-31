package com.hotelerp.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String employeeId;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String department;
    private String role;
    private String property;
    private String shift;
    private String status;
    /** Parsed back to a list for the consumer */
    private List<String> floorAccess;
    private String notes;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
