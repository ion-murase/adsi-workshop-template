package com.example.attendance.application.controller;

import com.example.attendance.application.service.PaidLeaveService;
import com.example.attendance.export.dto.PaidLeaveBalanceResponse;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/paid-leave")
public class PaidLeaveController {

    private final PaidLeaveService paidLeaveService;
    private final JwtTokenProvider jwtTokenProvider;

    public PaidLeaveController(PaidLeaveService paidLeaveService, JwtTokenProvider jwtTokenProvider) {
        this.paidLeaveService = paidLeaveService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/balance")
    public ResponseEntity<PaidLeaveBalanceResponse> getBalance(
            @RequestHeader("Authorization") String authHeader) {
        var userId = extractUserId(authHeader);
        var balance = paidLeaveService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

    private UUID extractUserId(String authHeader) {
        var token = authHeader.replace("Bearer ", "");
        var claims = jwtTokenProvider.parseToken(token);
        return UUID.fromString(claims.getSubject());
    }
}
