package com.example.attendance.export.service;

import java.util.UUID;

public interface CsvExportService {

    byte[] exportTimeRecords(UUID requesterId, UUID targetUserId, int year, int month);

    byte[] exportMonthlySummary(UUID requesterId, int year, int month);

    byte[] exportUsers(UUID requesterId);
}
