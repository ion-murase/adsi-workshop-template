package com.example.attendance.notification.service;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.notification.entity.Notification;
import com.example.attendance.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceImplTest {

    private NotificationRepository repository;
    private NotificationServiceImpl service;

    private static final UUID RECIPIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRepository.class);
        service = new NotificationServiceImpl(repository);
    }

    @Test
    @DisplayName("send: 通知が作成される")
    void send_createsNotification() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.send(RECIPIENT_ID, NotificationType.APPLICATION_APPROVED,
                "承認されました", "打刻修正申請が承認されました。",
                UUID.fromString("00000000-0000-0000-0000-000000000201"));

        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertThat(saved.getRecipientId()).isEqualTo(RECIPIENT_ID);
        assertThat(saved.getType()).isEqualTo(NotificationType.APPLICATION_APPROVED);
        assertThat(saved.getTitle()).isEqualTo("承認されました");
        assertThat(saved.getMessage()).isEqualTo("打刻修正申請が承認されました。");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    @DisplayName("getNotifications: ページネーション付き一覧を返す")
    void getNotifications_returnsPagedList() {
        var notification = createNotification();
        var page = new PageImpl<>(List.of(notification), PageRequest.of(0, 10), 1);
        when(repository.findByRecipientIdOrderByCreatedAtDesc(RECIPIENT_ID, PageRequest.of(0, 10)))
                .thenReturn(page);

        var result = service.getNotifications(RECIPIENT_ID, false, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("テスト通知");
    }

    @Test
    @DisplayName("getNotifications: 未読のみフィルタ")
    void getNotifications_unreadOnlyFilter() {
        var notification = createNotification();
        var page = new PageImpl<>(List.of(notification), PageRequest.of(0, 10), 1);
        when(repository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(RECIPIENT_ID, PageRequest.of(0, 10)))
                .thenReturn(page);

        var result = service.getNotifications(RECIPIENT_ID, true, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).isRead()).isFalse();
    }

    @Test
    @DisplayName("getUnreadCount: 未読件数を返す")
    void getUnreadCount_returnsCount() {
        when(repository.countByRecipientIdAndReadFalse(RECIPIENT_ID)).thenReturn(5L);

        var result = service.getUnreadCount(RECIPIENT_ID);

        assertThat(result.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("markAsRead: 既読化できる")
    void markAsRead_marksAsRead() {
        var notification = createNotification();
        when(repository.findById(notification.getId())).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.markAsRead(notification.getId(), RECIPIENT_ID);

        verify(repository).save(any(Notification.class));
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("markAsRead: 他人の通知は既読にできない")
    void markAsRead_otherRecipient_throwsException() {
        var notification = createNotification();
        when(repository.findById(notification.getId())).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.markAsRead(notification.getId(), OTHER_USER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("markAllAsRead: 全通知を既読にする")
    void markAllAsRead_marksAllAsRead() {
        when(repository.markAllAsReadByRecipientId(RECIPIENT_ID)).thenReturn(3);

        service.markAllAsRead(RECIPIENT_ID);

        verify(repository).markAllAsReadByRecipientId(RECIPIENT_ID);
    }

    private Notification createNotification() {
        return new Notification(
                RECIPIENT_ID,
                NotificationType.APPLICATION_APPROVED,
                "テスト通知",
                "テストメッセージ",
                UUID.randomUUID()
        );
    }
}
