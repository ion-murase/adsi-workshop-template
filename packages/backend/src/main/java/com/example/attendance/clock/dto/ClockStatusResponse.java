package com.example.attendance.clock.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record ClockStatusResponse(
        LocalDate workDate,
        ZonedDateTime clockIn,
        ZonedDateTime clockOut,
        String currentState,
        List<EntryRecord> entries
) {
    public record EntryRecord(String entryType, ZonedDateTime recordedAt) {}
}
