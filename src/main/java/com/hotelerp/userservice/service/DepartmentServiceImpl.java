package com.hotelerp.userservice.service;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.DepartmentRequest;
import com.hotelerp.userservice.dto.DepartmentResponse;
import com.hotelerp.userservice.entity.Department;
import com.hotelerp.userservice.repository.DepartmentRepository;
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
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<DepartmentResponse>> getAllDepartments() {
        log.info("Fetching all departments");
        List<DepartmentResponse> departments = departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return StandardResponse.success(departments, "Departments fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<DepartmentResponse> getDepartmentById(Long id) {
        log.info("Fetching department by ID: {}", id);
        return departmentRepository.findById(id)
                .map(dept -> StandardResponse.success(mapToResponse(dept), "Department fetched successfully"))
                .orElse(StandardResponse.error("Department not found", "NOT_FOUND", "id", String.valueOf(id)));
    }

    @Override
    @Transactional
    public StandardResponse<DepartmentResponse> createDepartment(DepartmentRequest request) {
        log.info("Creating new department: {}", request.getName());
        
        if (departmentRepository.findByName(request.getName()).isPresent()) {
            return StandardResponse.error("Department name already exists", "DUPLICATE_DEPT_NAME", "name", request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Department saved = departmentRepository.save(department);
        return StandardResponse.success(mapToResponse(saved), "Department created successfully");
    }

    @Override
    @Transactional
    public StandardResponse<DepartmentResponse> updateDepartment(Long id, DepartmentRequest request) {
        log.info("Updating department ID: {}", id);
        
        Optional<Department> deptOpt = departmentRepository.findById(id);
        if (deptOpt.isEmpty()) {
            return StandardResponse.error("Department not found", "NOT_FOUND", "id", String.valueOf(id));
        }

        Department department = deptOpt.get();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            department.setIsActive(request.getIsActive());
        }

        Department updated = departmentRepository.save(department);
        return StandardResponse.success(mapToResponse(updated), "Department updated successfully");
    }

    @Override
    @Transactional
    public StandardResponse<Void> deleteDepartment(Long id) {
        log.info("Deleting department ID: {}", id);
        if (!departmentRepository.existsById(id)) {
            return StandardResponse.error("Department not found", "NOT_FOUND", "id", String.valueOf(id));
        }
        departmentRepository.deleteById(id);
        return StandardResponse.success(null, "Department deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<DepartmentResponse>> getActiveDepartments() {
        log.info("Fetching active departments");
        List<DepartmentResponse> departments = departmentRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return StandardResponse.success(departments, "Active departments fetched successfully");
    }

    private DepartmentResponse mapToResponse(Department department) {
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
}
