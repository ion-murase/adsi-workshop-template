package com.example.attendance.clock.service;

import com.example.attendance.clock.dto.ClockStatusResponse;
import com.example.attendance.clock.dto.TimeRecordResponse;
import com.example.attendance.clock.dto.MonthlySummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ClockService {

    TimeRecordResponse clockIn(UUID userId);

    TimeRecordResponse clockOut(UUID userId);

    TimeRecordResponse goOut(UUID userId);

    TimeRecordResponse returnFromOut(UUID userId);

    ClockStatusResponse getStatus(UUID userId);

    List<TimeRecordResponse> getMonthlyRecords(UUID userId, int year, int month);

    MonthlySummaryResponse getMonthlySummary(UUID userId, int year, int month);
}
