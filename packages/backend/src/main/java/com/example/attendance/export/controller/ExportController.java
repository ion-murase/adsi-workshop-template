package com.example.attendance.export.controller;

import com.example.attendance.export.service.CsvExportService;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final CsvExportService csvExportService;
    private final JwtTokenProvider jwtTokenProvider;

    public ExportController(CsvExportService csvExportService, JwtTokenProvider jwtTokenProvider) {
        this.csvExportService = csvExportService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/time-records")
    public ResponseEntity<byte[]> exportTimeRecords(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) UUID userId,
            @RequestParam int year,
            @RequestParam int month) {
        var requesterId = extractUserId(authHeader);
        var targetUserId = userId != null ? userId : requesterId;
        var csv = csvExportService.exportTimeRecords(requesterId, targetUserId, year, month);
        return buildCsvResponse(csv, "time_records_" + year + "_" + month + ".csv");
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<byte[]> exportMonthlySummary(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int year,
            @RequestParam int month) {
        var requesterId = extractUserId(authHeader);
        var csv = csvExportService.exportMonthlySummary(requesterId, year, month);
        return buildCsvResponse(csv, "monthly_summary_" + year + "_" + month + ".csv");
    }

    @GetMapping("/users")
    public ResponseEntity<byte[]> exportUsers(
            @RequestHeader("Authorization") String authHeader) {
        var requesterId = extractUserId(authHeader);
        var csv = csvExportService.exportUsers(requesterId);
        return buildCsvResponse(csv, "users.csv");
    }

    private UUID extractUserId(String authHeader) {
        var token = authHeader.replace("Bearer ", "");
        var claims = jwtTokenProvider.parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    private ResponseEntity<byte[]> buildCsvResponse(byte[] csv, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
