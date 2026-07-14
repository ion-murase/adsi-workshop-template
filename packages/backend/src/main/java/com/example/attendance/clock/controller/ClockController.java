package com.example.attendance.clock.controller;

import com.example.attendance.clock.dto.ClockStatusResponse;
import com.example.attendance.clock.dto.TimeRecordResponse;
import com.example.attendance.clock.service.ClockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/clock")
public class ClockController {

    private final ClockService clockService;

    public ClockController(ClockService clockService) {
        this.clockService = clockService;
    }

    @PostMapping("/in")
    public ResponseEntity<TimeRecordResponse> clockIn(@RequestParam UUID userId) {
        var result = clockService.clockIn(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/out")
    public ResponseEntity<TimeRecordResponse> clockOut(@RequestParam UUID userId) {
        return ResponseEntity.ok(clockService.clockOut(userId));
    }

    @PostMapping("/go-out")
    public ResponseEntity<TimeRecordResponse> goOut(@RequestParam UUID userId) {
        return ResponseEntity.ok(clockService.goOut(userId));
    }

    @PostMapping("/return")
    public ResponseEntity<TimeRecordResponse> returnFromOut(@RequestParam UUID userId) {
        return ResponseEntity.ok(clockService.returnFromOut(userId));
    }

    @GetMapping("/status")
    public ResponseEntity<ClockStatusResponse> getStatus(@RequestParam UUID userId) {
        return ResponseEntity.ok(clockService.getStatus(userId));
    }
}
