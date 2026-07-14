package com.example.attendance.clock.controller;

import com.example.attendance.clock.dto.MonthlySummaryResponse;
import com.example.attendance.clock.dto.TimeRecordResponse;
import com.example.attendance.clock.service.ClockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-records")
public class TimeRecordController {

    private final ClockService clockService;

    public TimeRecordController(ClockService clockService) {
        this.clockService = clockService;
    }

    @GetMapping
    public ResponseEntity<List<TimeRecordResponse>> getMonthlyRecords(
            @RequestParam UUID userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(clockService.getMonthlyRecords(userId, year, month));
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam UUID userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(clockService.getMonthlySummary(userId, year, month));
    }
}
