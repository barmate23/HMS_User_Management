package com.hotelerp.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {
    private Long moduleId;
    private boolean canView;
    private boolean canCreate;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canApprove;
    private boolean canExport;
}
