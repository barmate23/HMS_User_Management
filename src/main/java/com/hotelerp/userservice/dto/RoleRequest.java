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
public class RoleRequest {
    private String name;
    private String department;
    private String accessLevel;
    private String status;
    private String description;
    private List<PermissionRequest> permissions;
}
