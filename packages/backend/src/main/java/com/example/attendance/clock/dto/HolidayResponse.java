package com.example.attendance.clock.dto;

import com.example.attendance.clock.entity.CompanyCalendar;

import java.time.LocalDate;
import java.util.UUID;

public record HolidayResponse(
        UUID id,
        LocalDate holidayDate,
        String holidayName,
        Integer fiscalYear
) {
    public static HolidayResponse from(CompanyCalendar calendar) {
        return new HolidayResponse(
                calendar.getId(),
                calendar.getHolidayDate(),
                calendar.getHolidayName(),
                calendar.getFiscalYear()
        );
    }
}
