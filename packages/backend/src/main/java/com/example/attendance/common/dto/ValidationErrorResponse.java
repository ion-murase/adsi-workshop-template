package com.example.attendance.common.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record ValidationErrorResponse(
        int status,
        String message,
        List<FieldError> errors,
        ZonedDateTime timestamp
) {
    public record FieldError(String field, String message) {}

    public static ValidationErrorResponse of(List<FieldError> errors) {
        return new ValidationErrorResponse(400, "Validation failed", errors, ZonedDateTime.now());
    }
}
