package com.example.attendance.organization.dto;

import com.example.attendance.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserRequest(
        @NotBlank @Email String email,
        @NotBlank String name,
        @NotNull Role role,
        @NotNull UUID primaryDepartmentId
) {}
