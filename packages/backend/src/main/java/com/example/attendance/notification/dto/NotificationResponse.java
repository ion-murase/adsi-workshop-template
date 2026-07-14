package com.example.attendance.notification.dto;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.notification.entity.Notification;

import java.time.ZonedDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        UUID referenceId,
        boolean isRead,
        ZonedDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
