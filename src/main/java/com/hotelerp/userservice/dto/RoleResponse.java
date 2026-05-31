package com.hotelerp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private String department;
    private String accessLevel;
    private String status;
    private String description;
    private List<PermissionResponse> permissions;
    private int userCount; // To match the "USERS 1" in the UI
    private int permissionCount; // To match the "PERMISSIONS 72" in the UI
}
