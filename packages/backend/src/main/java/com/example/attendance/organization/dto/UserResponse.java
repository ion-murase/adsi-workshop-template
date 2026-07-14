package com.example.attendance.organization.dto;

import com.example.attendance.common.enums.Role;
import com.example.attendance.organization.entity.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        Role role,
        UUID primaryDepartmentId,
        boolean active,
        boolean requirePasswordChange
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getPrimaryDepartmentId(),
                user.getActive(),
                user.getRequirePasswordChange()
        );
    }
}
