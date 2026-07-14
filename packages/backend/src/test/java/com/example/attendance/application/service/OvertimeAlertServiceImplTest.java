package com.example.attendance.application.service;

import com.example.attendance.clock.entity.TimeRecord;
import com.example.attendance.clock.repository.TimeRecordRepository;
import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.notification.service.NotificationService;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvertimeAlertServiceImplTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    private OvertimeAlertServiceImpl service;

    private UUID userId;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        service = new OvertimeAlertServiceImpl(timeRecordRepository, userRepository, notificationService);
        userId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
    }

    private User createUser(UUID id, Role role) {
        return User.builder()
                .email(id + "@example.com")
                .passwordHash("hashed")
                .name("ユーザ")
                .role(role)
                .primaryDepartmentId(departmentId)
                .active(true)
                .failedLoginAttempts(0)
                .requirePasswordChange(false)
                .build();
    }

    private TimeRecord createTimeRecordWithOvertime(int overtimeMinutes) {
        var record = new TimeRecord();
        record.setOvertimeMinutes(overtimeMinutes);
        return record;
    }

    @Test
    @DisplayName("残業30h超過: 通知が送信される")
    void checkAndNotify_over30h_sendsNotification() {
        var user = createUser(userId, Role.GENERAL);
        var approver = createUser(UUID.randomUUID(), Role.APPROVER);
        var yearMonth = YearMonth.now();

        // 1900分 = 31h40m (30h超過だが45h未満)
        var records = List.of(createTimeRecordWithOvertime(1900));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeRecordRepository.findByUserIdAndYearMonth(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        when(notificationService.existsForCurrentMonth(userId, NotificationType.OVERTIME_30,
                yearMonth.getYear(), yearMonth.getMonthValue())).thenReturn(false);
        when(userRepository.findByPrimaryDepartmentIdAndActiveOrderByNameAsc(departmentId, true))
                .thenReturn(List.of(user, approver));

        service.checkAndNotify(userId, yearMonth);

        verify(notificationService).send(eq(userId), eq(NotificationType.OVERTIME_30), any(), any(), any());
        verify(notificationService).send(eq(approver.getId()), eq(NotificationType.OVERTIME_30), any(), any(), any());
    }

    @Test
    @DisplayName("残業30h未満: 通知は送信されない")
    void checkAndNotify_under30h_noNotification() {
        var user = createUser(userId, Role.GENERAL);
        var yearMonth = YearMonth.now();

        var records = List.of(createTimeRecordWithOvertime(500));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeRecordRepository.findByUserIdAndYearMonth(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);

        service.checkAndNotify(userId, yearMonth);

        verify(notificationService, never()).send(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("重複通知防止: 同月同閾値で既に通知済みの場合はスキップ")
    void checkAndNotify_alreadyNotified_skips() {
        var user = createUser(userId, Role.GENERAL);
        var yearMonth = YearMonth.now();

        var records = List.of(createTimeRecordWithOvertime(1900));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeRecordRepository.findByUserIdAndYearMonth(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        when(notificationService.existsForCurrentMonth(userId, NotificationType.OVERTIME_30,
                yearMonth.getYear(), yearMonth.getMonthValue())).thenReturn(true);

        service.checkAndNotify(userId, yearMonth);

        verify(notificationService, never()).send(eq(userId), eq(NotificationType.OVERTIME_30), any(), any(), any());
    }

    @Test
    @DisplayName("残業45h超過: 30h既通知でも45h通知は送信される")
    void checkAndNotify_over45h_sends45Notification() {
        var user = createUser(userId, Role.GENERAL);
        var approver = createUser(UUID.randomUUID(), Role.APPROVER);
        var yearMonth = YearMonth.now();

        // 2800分 = 46h40m (45h超過だが60h未満)
        var records = List.of(createTimeRecordWithOvertime(2800));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeRecordRepository.findByUserIdAndYearMonth(eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(records);
        when(notificationService.existsForCurrentMonth(userId, NotificationType.OVERTIME_45,
                yearMonth.getYear(), yearMonth.getMonthValue())).thenReturn(false);
        when(notificationService.existsForCurrentMonth(userId, NotificationType.OVERTIME_30,
                yearMonth.getYear(), yearMonth.getMonthValue())).thenReturn(true);
        when(userRepository.findByPrimaryDepartmentIdAndActiveOrderByNameAsc(departmentId, true))
                .thenReturn(List.of(user, approver));

        service.checkAndNotify(userId, yearMonth);

        verify(notificationService).send(eq(userId), eq(NotificationType.OVERTIME_45), any(), any(), any());
        verify(notificationService, never()).send(eq(userId), eq(NotificationType.OVERTIME_30), any(), any(), any());
    }
}
