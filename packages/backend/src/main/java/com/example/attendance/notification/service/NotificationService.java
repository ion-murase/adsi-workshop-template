package com.example.attendance.notification.service;

import com.example.attendance.common.enums.NotificationType;

import java.util.UUID;

public interface NotificationService {

    void send(UUID recipientId, NotificationType type, String title, String message, UUID referenceId);

    boolean existsForCurrentMonth(UUID recipientId, NotificationType type, int year, int month);
}
