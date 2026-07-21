package com.example.attendance.notification.service;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.notification.dto.NotificationResponse;
import com.example.attendance.notification.dto.UnreadCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    void send(UUID recipientId, NotificationType type, String title, String message, UUID referenceId);

    Page<NotificationResponse> getNotifications(UUID recipientId, boolean unreadOnly, Pageable pageable);

    UnreadCountResponse getUnreadCount(UUID recipientId);

    void markAsRead(UUID notificationId, UUID recipientId);

    void markAllAsRead(UUID recipientId);

    boolean existsForCurrentMonth(UUID recipientId, NotificationType type, int year, int month);
}
