package com.example.attendance.notification.service;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.notification.entity.Notification;
import com.example.attendance.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void send(UUID recipientId, NotificationType type, String title, String message, UUID referenceId) {
        var notification = new Notification(recipientId, type, title, message, referenceId);
        notificationRepository.save(notification);
        log.info("Notification sent: type={}, recipient={}", type, recipientId);
    }

    @Override
    public boolean existsForCurrentMonth(UUID recipientId, NotificationType type, int year, int month) {
        var monthStart = LocalDate.of(year, month, 1).atStartOfDay(TOKYO);
        return notificationRepository.existsByRecipientIdAndTypeAndCreatedAtAfter(recipientId, type, monthStart);
    }
}
