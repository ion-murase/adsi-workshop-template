package com.example.attendance.clock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HolidayRequest(
        @NotNull LocalDate holidayDate,
        @NotBlank String holidayName
) {}
