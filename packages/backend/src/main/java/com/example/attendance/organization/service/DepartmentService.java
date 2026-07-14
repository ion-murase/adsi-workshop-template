package com.example.attendance.organization.service;

import com.example.attendance.organization.dto.DepartmentRequest;
import com.example.attendance.organization.dto.DepartmentResponse;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse updateDepartment(UUID id, DepartmentRequest request);

    void deleteDepartment(UUID id);
}
