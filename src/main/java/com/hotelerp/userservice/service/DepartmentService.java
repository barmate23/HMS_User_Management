package com.hotelerp.userservice.service;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.DepartmentRequest;
import com.hotelerp.userservice.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    StandardResponse<List<DepartmentResponse>> getAllDepartments();

    StandardResponse<DepartmentResponse> getDepartmentById(Long id);

    StandardResponse<DepartmentResponse> createDepartment(DepartmentRequest request);

    StandardResponse<DepartmentResponse> updateDepartment(Long id, DepartmentRequest request);

    StandardResponse<Void> deleteDepartment(Long id);

    StandardResponse<List<DepartmentResponse>> getActiveDepartments();
}