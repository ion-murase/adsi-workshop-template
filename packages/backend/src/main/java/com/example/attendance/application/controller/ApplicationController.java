package com.example.attendance.application.controller;

import com.example.attendance.application.dto.ApplicationResponse;
import com.example.attendance.application.dto.ClockFixRequest;
import com.example.attendance.application.dto.RejectRequest;
import com.example.attendance.application.service.ApplicationService;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JwtTokenProvider jwtTokenProvider;

    public ApplicationController(ApplicationService applicationService, JwtTokenProvider jwtTokenProvider) {
        this.applicationService = applicationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/clock-fix")
    public ResponseEntity<ApplicationResponse> submitClockFix(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ClockFixRequest request) {
        var userId = extractUserId(authHeader);
        var response = applicationService.submitClockFix(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var userId = extractUserId(authHeader);
        var result = applicationService.getMyApplications(userId, status, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<ApplicationResponse>> getPendingApplications(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var userId = extractUserId(authHeader);
        var result = applicationService.getPendingApplications(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApplicationResponse> approve(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {
        var userId = extractUserId(authHeader);
        var response = applicationService.approve(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApplicationResponse> reject(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @Valid @RequestBody RejectRequest request) {
        var userId = extractUserId(authHeader);
        var response = applicationService.reject(id, userId, request.comment());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ApplicationResponse> withdraw(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {
        var userId = extractUserId(authHeader);
        var response = applicationService.withdraw(id, userId);
        return ResponseEntity.ok(response);
    }

    private UUID extractUserId(String authHeader) {
        var token = authHeader.replace("Bearer ", "");
        var claims = jwtTokenProvider.parseToken(token);
        return UUID.fromString(claims.getSubject());
    }
}
