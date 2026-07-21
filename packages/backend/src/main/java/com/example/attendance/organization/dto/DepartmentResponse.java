package com.example.attendance.organization.dto;

import com.example.attendance.organization.entity.Department;

import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        String name,
        UUID siteId
) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getSiteId()
        );
    }
}
