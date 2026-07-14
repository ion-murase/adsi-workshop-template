package com.example.attendance.clock.dto;

import com.example.attendance.clock.entity.TimeRecord;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public record TimeRecordResponse(
        UUID id,
        LocalDate workDate,
        ZonedDateTime clockIn,
        ZonedDateTime clockOut,
        Integer breakMinutes,
        Integer workMinutes,
        Integer overtimeMinutes,
        Integer nightMinutes,
        Integer holidayWorkMinutes,
        Boolean isHoliday
) {
    public static TimeRecordResponse from(TimeRecord record) {
        return new TimeRecordResponse(
                record.getId(),
                record.getWorkDate(),
                record.getClockIn(),
                record.getClockOut(),
                record.getBreakMinutes(),
                record.getWorkMinutes(),
                record.getOvertimeMinutes(),
                record.getNightMinutes(),
                record.getHolidayWorkMinutes(),
                record.getIsHoliday()
        );
    }
}
