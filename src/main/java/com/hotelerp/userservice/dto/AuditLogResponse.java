package com.hotelerp.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String role;
    private String activity;
    private String module;
    private String ipAddress;
    private String severity;
    private LocalDateTime timestamp;
}
