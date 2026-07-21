package com.example.attendance.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record ClockFixRequest(
        @NotNull(message = "対象日は必須です")
        LocalDate targetDate,

        ZonedDateTime correctedClockIn,

        ZonedDateTime correctedClockOut,

        @NotBlank(message = "理由は必須です")
        @Size(max = 1000, message = "理由は1000文字以内で入力してください")
        String reason
) {}
