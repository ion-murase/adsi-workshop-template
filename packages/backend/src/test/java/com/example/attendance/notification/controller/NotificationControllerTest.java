package com.example.attendance.notification.controller;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.infrastructure.security.JwtAuthFilter;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import com.example.attendance.notification.dto.NotificationResponse;
import com.example.attendance.notification.dto.UnreadCountResponse;
import com.example.attendance.notification.service.NotificationService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}),
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID NOTIFICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        var claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(USER_ID.toString());
        when(jwtTokenProvider.parseToken("test-token")).thenReturn(claims);
    }

    @Test
    @DisplayName("GET /api/notifications: 通知一覧を返す")
    void getNotifications_returnsPagedList() throws Exception {
        var response = new NotificationResponse(
                NOTIFICATION_ID,
                NotificationType.APPLICATION_APPROVED,
                "承認されました",
                "打刻修正が承認されました。",
                UUID.randomUUID(),
                false,
                ZonedDateTime.now()
        );
        var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(notificationService.getNotifications(eq(USER_ID), anyBoolean(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("承認されました"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count: 未読件数を返す")
    void getUnreadCount_returnsCount() throws Exception {
        when(notificationService.getUnreadCount(eq(USER_ID)))
                .thenReturn(new UnreadCountResponse(3));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read: 既読化する")
    void markAsRead_returns200() throws Exception {
        mockMvc.perform(post("/api/notifications/" + NOTIFICATION_ID + "/read")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead(eq(NOTIFICATION_ID), eq(USER_ID));
    }

    @Test
    @DisplayName("POST /api/notifications/read-all: 全既読化する")
    void markAllAsRead_returns200() throws Exception {
        mockMvc.perform(post("/api/notifications/read-all")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk());

        verify(notificationService).markAllAsRead(eq(USER_ID));
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read: 存在しない通知は404")
    void markAsRead_notFound_returns400() throws Exception {
        var unknownId = UUID.randomUUID();
        when(notificationService.getUnreadCount(any())).thenReturn(new UnreadCountResponse(0));
        org.mockito.Mockito.doThrow(new BusinessException("通知が見つかりません"))
                .when(notificationService).markAsRead(eq(unknownId), eq(USER_ID));

        mockMvc.perform(post("/api/notifications/" + unknownId + "/read")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isBadRequest());
    }
}
