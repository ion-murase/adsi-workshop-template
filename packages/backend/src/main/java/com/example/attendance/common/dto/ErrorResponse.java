package com.example.attendance.common.dto;

import java.time.ZonedDateTime;

public record ErrorResponse(
        int status,
        String message,
        ZonedDateTime timestamp
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, ZonedDateTime.now());
    }
}
