package com.hotelerp.userservice.service;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.*;
import com.hotelerp.userservice.entity.Department;
import com.hotelerp.userservice.entity.Module;
import com.hotelerp.userservice.entity.Role;
import com.hotelerp.userservice.entity.RolePermission;
import com.hotelerp.userservice.repository.DepartmentRepository;
import com.hotelerp.userservice.repository.ModuleRepository;
import com.hotelerp.userservice.repository.RoleRepository;
import com.hotelerp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ModuleResponse>> getAllModules() {
        log.info("Fetching all active modules");
        List<ModuleResponse> modules = moduleRepository.findByActiveTrue().stream()
                .map(m -> ModuleResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .category(m.getCategory())
                        .build())
                .collect(Collectors.toList());
        return StandardResponse.success(modules, "Modules fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<RoleResponse>> getAllRoles(Long departmentId) {
        log.info("Fetching all roles for department: {}", departmentId);
        List<Role> roles;
        if (departmentId != null) {
            roles = roleRepository.findByDepartmentId(departmentId);
        } else {
            roles = roleRepository.findAll();
        }
        
        List<RoleResponse> responses = roles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
                
        return StandardResponse.success(responses, "Roles fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<RoleResponse> getRoleById(Long id) {
        log.info("Fetching role by ID: {}", id);
        return roleRepository.findById(id)
                .map(role -> StandardResponse.success(mapToResponse(role), "Role fetched successfully"))
                .orElse(StandardResponse.error("Role not found", "NOT_FOUND", "id", String.valueOf(id)));
    }

    @Override
    @Transactional
    public StandardResponse<RoleResponse> createRole(RoleRequest request) {
        log.info("Creating new role: {}", request.getName());
        
        if (roleRepository.existsByName(request.getName())) {
            return StandardResponse.error("Role name already exists", "DUPLICATE_ROLE_NAME", "name", request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .department(request.getDepartmentId() != null 
                        ? departmentRepository.findById(request.getDepartmentId()).orElse(null) : null)
                .accessLevel(request.getAccessLevel())
                .status(request.getStatus())
                .description(request.getDescription())
                .build();

        if (request.getPermissions() != null) {
            List<RolePermission> permissions = request.getPermissions().stream()
                    .map(pReq -> {
                        Module module = moduleRepository.findById(pReq.getModuleId())
                                .orElseThrow(() -> new RuntimeException("Module not found: " + pReq.getModuleId()));
                        return RolePermission.builder()
                                .role(role)
                                .module(module)
                                .canView(pReq.isCanView())
                                .canCreate(pReq.isCanCreate())
                                .canEdit(pReq.isCanEdit())
                                .canDelete(pReq.isCanDelete())
                                .canApprove(pReq.isCanApprove())
                                .canExport(pReq.isCanExport())
                                .build();
                    })
                    .collect(Collectors.toList());
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        return StandardResponse.success(mapToResponse(savedRole), "Role created successfully");
    }

    @Override
    @Transactional
    public StandardResponse<RoleResponse> updateRole(Long id, RoleRequest request) {
        log.info("Updating role ID: {}", id);
        
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            return StandardResponse.error("Role not found", "NOT_FOUND", "id", String.valueOf(id));
        }

        Role role = roleOpt.get();
        
        // Update basic fields
        role.setName(request.getName());
        if (request.getDepartmentId() != null) {
            role.setDepartment(departmentRepository.findById(request.getDepartmentId()).orElse(null));
        }
        role.setAccessLevel(request.getAccessLevel());
        role.setStatus(request.getStatus());
        role.setDescription(request.getDescription());

        // Update permissions (clear and rebuild for simplicity in this implementation)
        role.getPermissions().clear();
        if (request.getPermissions() != null) {
            List<RolePermission> newPermissions = request.getPermissions().stream()
                    .map(pReq -> {
                        Module module = moduleRepository.findById(pReq.getModuleId())
                                .orElseThrow(() -> new RuntimeException("Module not found: " + pReq.getModuleId()));
                        return RolePermission.builder()
                                .role(role)
                                .module(module)
                                .canView(pReq.isCanView())
                                .canCreate(pReq.isCanCreate())
                                .canEdit(pReq.isCanEdit())
                                .canDelete(pReq.isCanDelete())
                                .canApprove(pReq.isCanApprove())
                                .canExport(pReq.isCanExport())
                                .build();
                    })
                    .collect(Collectors.toList());
            role.getPermissions().addAll(newPermissions);
        }

        Role updatedRole = roleRepository.save(role);
        return StandardResponse.success(mapToResponse(updatedRole), "Role updated successfully");
    }

    @Override
    @Transactional
    public StandardResponse<Void> deleteRole(Long id) {
        log.info("Deleting role ID: {}", id);
        if (!roleRepository.existsById(id)) {
            return StandardResponse.error("Role not found", "NOT_FOUND", "id", String.valueOf(id));
        }
        roleRepository.deleteById(id);
        return StandardResponse.success(null, "Role deleted successfully");
    }

    private DepartmentResponse mapToDepartmentResponse(Department department) {
        if (department == null) return null;
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .isActive(department.getIsActive())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    private RoleResponse mapToResponse(Role role) {
        List<PermissionResponse> permissions = role.getPermissions().stream()
                .map(p -> PermissionResponse.builder()
                        .id(p.getId())
                        .moduleId(p.getModule().getId())
                        .moduleName(p.getModule().getName())
                        .category(p.getModule().getCategory())
                        .canView(p.isCanView())
                        .canCreate(p.isCanCreate())
                        .canEdit(p.isCanEdit())
                        .canDelete(p.isCanDelete())
                        .canApprove(p.isCanApprove())
                        .canExport(p.isCanExport())
                        .build())
                .collect(Collectors.toList());

        // Calculate total permissions (sum of all enabled booleans)
        int permissionCount = (int) role.getPermissions().stream()
                .mapToLong(p -> (p.isCanView() ? 1 : 0) + (p.isCanCreate() ? 1 : 0) + (p.isCanEdit() ? 1 : 0) + 
                                (p.isCanDelete() ? 1 : 0) + (p.isCanApprove() ? 1 : 0) + (p.isCanExport() ? 1 : 0))
                .sum();

        // Get user count for this role
        long userCount = userRepository.countByRoleName(role.getName());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .department(mapToDepartmentResponse(role.getDepartment()))
                .accessLevel(role.getAccessLevel())
                .status(role.getStatus())
                .description(role.getDescription())
                .permissions(permissions)
                .userCount((int) userCount)
                .permissionCount(permissionCount)
                .build();
    }
}
