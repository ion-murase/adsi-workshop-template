package com.example.attendance.notification.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/test-data-notification.sql")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private static final UUID RECIPIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Test
    @DisplayName("findByRecipientId: ページネーション付きで全通知を取得できる")
    void findByRecipientId_paginated_returnsAll() {
        var page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                RECIPIENT_ID, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getCreatedAt())
                .isAfterOrEqualTo(page.getContent().get(1).getCreatedAt());
    }

    @Test
    @DisplayName("findByRecipientId: ページサイズ制限が効く")
    void findByRecipientId_pageSizeLimit_returnsLimited() {
        var page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                RECIPIENT_ID, PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByRecipientIdAndReadFalse: 未読のみ取得")
    void findByRecipientIdAndReadFalse_returnsUnreadOnly() {
        var page = notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(
                RECIPIENT_ID, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(n -> !n.isRead());
    }

    @Test
    @DisplayName("countByRecipientIdAndReadFalse: 未読件数を返す")
    void countByRecipientIdAndReadFalse_returnsCount() {
        var count = notificationRepository.countByRecipientIdAndReadFalse(RECIPIENT_ID);
        assertThat(count).isEqualTo(2);
    }
}
