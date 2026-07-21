package com.example.attendance.notification;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import com.example.attendance.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@Sql("/test-data-notification.sql")
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String USER_ID = "00000000-0000-0000-0000-000000000010";
    private static final String NOTIFICATION_UNREAD_1 = "00000000-0000-0000-0000-000000000101";
    private static final String NOTIFICATION_UNREAD_2 = "00000000-0000-0000-0000-000000000102";

    private String authHeader;

    @BeforeEach
    void setUp() {
        var token = jwtTokenProvider.generateToken(UUID.fromString(USER_ID), "test@example.com", "GENERAL");
        authHeader = "Bearer " + token;
    }

    @Test
    @DisplayName("通知一覧取得: 全通知がページネーション付きで返る")
    void getNotifications_returnsAll() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].title").exists());
    }

    @Test
    @DisplayName("未読件数取得: 未読2件が返る")
    void getUnreadCount_returns2() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("個別既読化→未読件数が減る")
    void markAsRead_decreasesUnreadCount() throws Exception {
        mockMvc.perform(post("/api/notifications/" + NOTIFICATION_UNREAD_1 + "/read")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("全既読化→未読件数が0になる")
    void markAllAsRead_setsCountToZero() throws Exception {
        mockMvc.perform(post("/api/notifications/read-all")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("Service経由で通知を送信→一覧に表示される")
    void send_thenAppearInList() throws Exception {
        notificationService.send(
                UUID.fromString(USER_ID),
                NotificationType.OVERTIME_45,
                "残業45時間超過",
                "今月の残業が45時間を超えました。",
                null);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4));
    }
}
