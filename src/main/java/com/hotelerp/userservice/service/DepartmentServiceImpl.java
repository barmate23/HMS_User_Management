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
    private final com.hotelerp.userservice.repository.HotelRepository hotelRepository;
    private final com.hotelerp.userservice.config.LoginUser loginUser;

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<DepartmentResponse>> getAllDepartments() {
        Long hotelId = loginUser != null ? loginUser.getHotelId() : null;
        log.info("Fetching all departments for hotel ID: {}", hotelId);
        List<DepartmentResponse> departments = departmentRepository.findAllByPropertyId(hotelId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return StandardResponse.success(departments, "Departments fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<DepartmentResponse> getDepartmentById(Long id) {
        Long hotelId = loginUser != null ? loginUser.getHotelId() : null;
        log.info("Fetching department by ID: {} for hotel ID: {}", id, hotelId);
        Optional<Department> deptOpt = departmentRepository.findById(id);
        if (deptOpt.isEmpty() || (hotelId != null && deptOpt.get().getProperty() != null && !hotelId.equals(deptOpt.get().getProperty().getId()))) {
            return StandardResponse.error("Department not found", "NOT_FOUND", "id", String.valueOf(id));
        }
        return StandardResponse.success(mapToResponse(deptOpt.get()), "Department fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<DepartmentResponse> createDepartment(DepartmentRequest request) {
        Long hotelId = loginUser != null ? loginUser.getHotelId() : null;
        log.info("Creating new department: {} for hotel ID: {}", request.getName(), hotelId);
        
        if (departmentRepository.findByNameAndPropertyId(request.getName(), hotelId).isPresent()) {
            return StandardResponse.error("Department name already exists", "DUPLICATE_DEPT_NAME", "name", request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .property(hotelId != null ? hotelRepository.findById(hotelId).orElse(null) : null)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Department saved = departmentRepository.save(department);
        return StandardResponse.success(mapToResponse(saved), "Department created successfully");
    }

    @Override
    @Transactional
    public StandardResponse<DepartmentResponse> updateDepartment(Long id, DepartmentRequest request) {
        Long hotelId = loginUser != null ? loginUser.getHotelId() : null;
        log.info("Updating department ID: {} for hotel ID: {}", id, hotelId);
        
        Optional<Department> deptOpt = departmentRepository.findById(id);
        if (deptOpt.isEmpty() || (hotelId != null && deptOpt.get().getProperty() != null && !hotelId.equals(deptOpt.get().getProperty().getId()))) {
            return StandardResponse.error("Department not found", "NOT_FOUND", "id", String.valueOf(id));
        }

        Optional<Department> existingWithName = departmentRepository.findByNameAndPropertyId(request.getName(), hotelId);
        if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
            return StandardResponse.error("Department name already exists", "DUPLICATE_DEPT_NAME", "name", request.getName());
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
        Long hotelId = loginUser != null ? loginUser.getHotelId() : null;
        log.info("Deleting department ID: {} for hotel ID: {}", id, hotelId);
        
        Optional<Department> deptOpt = departmentRepository.findById(id);
        if (deptOpt.isEmpty() || (hotelId != null && deptOpt.get().getProperty() != null && !hotelId.equals(deptOpt.get().getProperty().getId()))) {
            return StandardResponse.error("Department not found", "NOT_FOUND", "id", String.valueOf(id));
        }

        departmentRepository.deleteById(id);
        return StandardResponse.success(null, "Department deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<DepartmentResponse>> getActiveDepartments() {
        Long hotelId = loginUser != null ? loginUser.getHotelId() : null;
        log.info("Fetching active departments for hotel ID: {}", hotelId);
        List<DepartmentResponse> departments = departmentRepository.findActiveByPropertyId(hotelId).stream()
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
                .hotelId(department.getProperty() != null ? department.getProperty().getId() : null)
                .hotelName(department.getProperty() != null ? department.getProperty().getName() : null)
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
