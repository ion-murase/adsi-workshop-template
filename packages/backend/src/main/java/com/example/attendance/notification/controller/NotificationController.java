package com.example.attendance.notification.controller;

import com.example.attendance.notification.dto.NotificationResponse;
import com.example.attendance.notification.dto.UnreadCountResponse;
import com.example.attendance.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        var page = notificationService.getNotifications(userId, unreadOnly, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@RequestParam UUID userId) {
        var result = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
