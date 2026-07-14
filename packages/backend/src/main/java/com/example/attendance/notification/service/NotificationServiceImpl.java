package com.example.attendance.notification.service;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.notification.dto.NotificationResponse;
import com.example.attendance.notification.dto.UnreadCountResponse;
import com.example.attendance.notification.entity.Notification;
import com.example.attendance.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void send(UUID recipientId, NotificationType type, String title, String message, UUID referenceId) {
        var notification = Notification.builder()
                .recipientId(recipientId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        repository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UUID recipientId, boolean unreadOnly, Pageable pageable) {
        Page<Notification> page;
        if (unreadOnly) {
            page = repository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId, pageable);
        } else {
            page = repository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
        }
        return page.map(NotificationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID recipientId) {
        long count = repository.countByRecipientIdAndIsReadFalse(recipientId);
        return new UnreadCountResponse(count);
    }

    @Override
    public void markAsRead(UUID notificationId, UUID recipientId) {
        var notification = repository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("通知が見つかりません"));

        if (!notification.getRecipientId().equals(recipientId)) {
            throw new BusinessException("他のユーザーの通知を操作できません");
        }

        notification.setRead(true);
        repository.save(notification);
    }

    @Override
    public void markAllAsRead(UUID recipientId) {
        repository.markAllAsReadByRecipientId(recipientId);
    }
}
