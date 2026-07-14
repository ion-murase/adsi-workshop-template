package com.example.attendance.clock.service;

import com.example.attendance.clock.dto.HolidayResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CalendarService {

    List<HolidayResponse> getHolidays(int year);

    boolean isHoliday(LocalDate date);

    HolidayResponse createHoliday(LocalDate date, String name);

    void deleteHoliday(UUID id);
}
