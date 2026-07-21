package com.example.attendance.clock.dto;

public record MonthlySummaryResponse(
        int year,
        int month,
        int workDays,
        int totalWorkMinutes,
        int totalOvertimeMinutes,
        int totalNightMinutes,
        int totalHolidayWorkMinutes
) {}
