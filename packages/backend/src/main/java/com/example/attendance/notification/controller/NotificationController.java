package com.example.attendance.notification.controller;

import com.example.attendance.infrastructure.security.JwtTokenProvider;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    public NotificationController(NotificationService notificationService, JwtTokenProvider jwtTokenProvider) {
        this.notificationService = notificationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        var userId = extractUserId(authHeader);
        var page = notificationService.getNotifications(userId, unreadOnly, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {
        var userId = extractUserId(authHeader);
        var result = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {
        var userId = extractUserId(authHeader);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("Authorization") String authHeader) {
        var userId = extractUserId(authHeader);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    private UUID extractUserId(String authHeader) {
        var token = authHeader.replace("Bearer ", "");
        var claims = jwtTokenProvider.parseToken(token);
        return UUID.fromString(claims.getSubject());
    }
}
