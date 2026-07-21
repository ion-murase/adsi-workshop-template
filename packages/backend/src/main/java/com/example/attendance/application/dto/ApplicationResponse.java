package com.example.attendance.application.dto;

import com.example.attendance.application.entity.Application;
import com.example.attendance.common.enums.ApplicationStatus;
import com.example.attendance.common.enums.ApplicationType;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID applicantId,
        String applicantName,
        UUID approverId,
        ApplicationType type,
        ApplicationStatus status,
        String rejectionComment,
        ZonedDateTime appliedAt,
        ZonedDateTime decidedAt,
        ClockFixDetailResponse clockFixDetail
) {
    public record ClockFixDetailResponse(
            LocalDate targetDate,
            ZonedDateTime correctedClockIn,
            ZonedDateTime correctedClockOut,
            String reason
    ) {}

    public static ApplicationResponse from(Application app, String applicantName) {
        ClockFixDetailResponse detail = null;
        if (app.getClockFixDetail() != null) {
            var d = app.getClockFixDetail();
            detail = new ClockFixDetailResponse(
                    d.getTargetDate(),
                    d.getCorrectedClockIn(),
                    d.getCorrectedClockOut(),
                    d.getReason()
            );
        }
        return new ApplicationResponse(
                app.getId(),
                app.getApplicantId(),
                applicantName,
                app.getApproverId(),
                app.getType(),
                app.getStatus(),
                app.getRejectionComment(),
                app.getAppliedAt(),
                app.getDecidedAt(),
                detail
        );
    }
}
