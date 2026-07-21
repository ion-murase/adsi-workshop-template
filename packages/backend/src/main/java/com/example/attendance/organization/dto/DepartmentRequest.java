package com.example.attendance.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DepartmentRequest(
        @NotBlank String name,
        @NotNull UUID siteId
) {}
