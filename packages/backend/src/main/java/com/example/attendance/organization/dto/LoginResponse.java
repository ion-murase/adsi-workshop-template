package com.example.attendance.organization.dto;

import com.example.attendance.common.enums.Role;

public record LoginResponse(
        String token,
        String userId,
        String name,
        String email,
        Role role,
        boolean requirePasswordChange
) {}
