package com.example.attendance.application.service;

import com.example.attendance.clock.repository.TimeRecordRepository;
import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.notification.service.NotificationService;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.UUID;

@Service
@Slf4j
public class OvertimeAlertServiceImpl implements OvertimeAlertService {

    private static final int THRESHOLD_30H = 30 * 60;
    private static final int THRESHOLD_45H = 45 * 60;
    private static final int THRESHOLD_60H = 60 * 60;

    private final TimeRecordRepository timeRecordRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public OvertimeAlertServiceImpl(TimeRecordRepository timeRecordRepository,
                                    UserRepository userRepository,
                                    NotificationService notificationService) {
        this.timeRecordRepository = timeRecordRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void checkAndNotify(UUID userId, YearMonth yearMonth) {
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        var startDate = yearMonth.atDay(1);
        var endDate = yearMonth.atEndOfMonth();
        var records = timeRecordRepository.findByUserIdAndYearMonth(userId, startDate, endDate);

        int totalOvertimeMinutes = records.stream()
                .mapToInt(r -> r.getOvertimeMinutes() != null ? r.getOvertimeMinutes() : 0)
                .sum();

        if (totalOvertimeMinutes < THRESHOLD_30H) {
            return;
        }

        checkThreshold(userId, user, yearMonth, totalOvertimeMinutes, THRESHOLD_60H, NotificationType.OVERTIME_60, "60時間");
        checkThreshold(userId, user, yearMonth, totalOvertimeMinutes, THRESHOLD_45H, NotificationType.OVERTIME_45, "45時間");
        checkThreshold(userId, user, yearMonth, totalOvertimeMinutes, THRESHOLD_30H, NotificationType.OVERTIME_30, "30時間");
    }

    private void checkThreshold(UUID userId, User user, YearMonth yearMonth,
                                int totalMinutes, int threshold,
                                NotificationType type, String label) {
        if (totalMinutes < threshold) {
            return;
        }

        if (notificationService.existsForCurrentMonth(userId, type, yearMonth.getYear(), yearMonth.getMonthValue())) {
            return;
        }

        var title = "残業時間が" + label + "を超過しました";
        var message = user.getName() + "さんの" + yearMonth.getYear() + "年" +
                yearMonth.getMonthValue() + "月の残業時間が" + label + "を超過しました（" +
                (totalMinutes / 60) + "時間" + (totalMinutes % 60) + "分）";

        notificationService.send(userId, type, title, message, null);

        var deptMembers = userRepository.findByPrimaryDepartmentIdAndActiveOrderByNameAsc(
                user.getPrimaryDepartmentId(), true);
        deptMembers.stream()
                .filter(u -> (u.getRole() == Role.APPROVER || u.getRole() == Role.ADMIN) && !u.getId().equals(userId))
                .forEach(approver -> notificationService.send(approver.getId(), type, title, message, null));

        log.info("Overtime alert sent: user={}, type={}, minutes={}", userId, type, totalMinutes);
    }
}
