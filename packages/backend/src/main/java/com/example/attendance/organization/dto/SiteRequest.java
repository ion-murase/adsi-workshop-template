package com.example.attendance.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record SiteRequest(
        @NotBlank String name,
        @NotBlank String timezone
) {}
