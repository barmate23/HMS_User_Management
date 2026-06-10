package com.hotelerp.userservice.controller;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.ModuleResponse;
import com.hotelerp.userservice.dto.RoleRequest;
import com.hotelerp.userservice.dto.RoleResponse;
import com.hotelerp.userservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hmsUserService/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/getAllModules")
    public StandardResponse<List<ModuleResponse>> getAllModules() {
        return roleService.getAllModules();
    }

    @GetMapping("/getAllRoles")
    public StandardResponse<List<RoleResponse>> getAllRoles(@RequestParam(required = false) Long departmentId) {
        return roleService.getAllRoles(departmentId);
    }

    @GetMapping("/getRoleById/{id}")
    public StandardResponse<RoleResponse> getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    @PostMapping("/createRole")
    public StandardResponse<RoleResponse> createRole(@RequestBody RoleRequest request) {
        return roleService.createRole(request);
    }

    @PutMapping("/updateRole/{id}")
    public StandardResponse<RoleResponse> updateRole(@PathVariable Long id, @RequestBody RoleRequest request) {
        return roleService.updateRole(id, request);
    }

    @DeleteMapping("/deleteRole/{id}")
    public StandardResponse<Void> deleteRole(@PathVariable Long id) {
        return roleService.deleteRole(id);
    }
}
