package com.example.attendance.notification.repository;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(UUID recipientId);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    boolean existsByRecipientIdAndTypeAndCreatedAtAfter(UUID recipientId, NotificationType type, ZonedDateTime after);
}
