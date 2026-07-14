package com.example.attendance.clock.controller;

import com.example.attendance.clock.dto.HolidayRequest;
import com.example.attendance.clock.dto.HolidayResponse;
import com.example.attendance.clock.service.CalendarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendar/holidays")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public ResponseEntity<List<HolidayResponse>> getHolidays(@RequestParam int year) {
        return ResponseEntity.ok(calendarService.getHolidays(year));
    }

    @PostMapping
    public ResponseEntity<HolidayResponse> createHoliday(@Valid @RequestBody HolidayRequest request) {
        var result = calendarService.createHoliday(request.holidayDate(), request.holidayName());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable UUID id) {
        calendarService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }
}
