package com.hotelerp.userservice.controller;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.DepartmentRequest;
import com.hotelerp.userservice.dto.DepartmentResponse;
import com.hotelerp.userservice.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hmsUserService/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/getAllDepartments")
    public StandardResponse<List<DepartmentResponse>> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @GetMapping("/getActiveDepartments")
    public StandardResponse<List<DepartmentResponse>> getActiveDepartments() {
        return departmentService.getActiveDepartments();
    }

    @GetMapping("/getDepartmentById/{id}")
    public StandardResponse<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    @PostMapping("/createDepartment")
    public StandardResponse<DepartmentResponse> createDepartment(@RequestBody DepartmentRequest request) {
        return departmentService.createDepartment(request);
    }

    @PutMapping("/updateDepartment/{id}")
    public StandardResponse<DepartmentResponse> updateDepartment(@PathVariable Long id, @RequestBody DepartmentRequest request) {
        return departmentService.updateDepartment(id, request);
    }

    @DeleteMapping("/deleteDepartment/{id}")
    public StandardResponse<Void> deleteDepartment(@PathVariable Long id) {
        return departmentService.deleteDepartment(id);
    }
}
