package com.example.attendance.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRequest(
        @NotNull(message = "休暇日は必須です")
        LocalDate leaveDate,

        @NotNull(message = "休暇種別は必須です")
        String leaveType
) {}
