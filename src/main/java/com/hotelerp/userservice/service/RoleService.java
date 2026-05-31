package com.hotelerp.userservice.service;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.ModuleResponse;
import com.hotelerp.userservice.dto.RoleRequest;
import com.hotelerp.userservice.dto.RoleResponse;
import java.util.List;

public interface RoleService {
    StandardResponse<List<ModuleResponse>> getAllModules();
    StandardResponse<List<RoleResponse>> getAllRoles();
    StandardResponse<RoleResponse> getRoleById(Long id);
    StandardResponse<RoleResponse> createRole(RoleRequest request);
    StandardResponse<RoleResponse> updateRole(Long id, RoleRequest request);
    StandardResponse<Void> deleteRole(Long id);
}
