package com.example.attendance.application.service;

import com.example.attendance.application.dto.ClockFixRequest;
import com.example.attendance.application.entity.Application;
import com.example.attendance.application.entity.ClockFixDetail;
import com.example.attendance.application.repository.ApplicationRepository;
import com.example.attendance.clock.entity.TimeRecord;
import com.example.attendance.clock.repository.CompanyCalendarRepository;
import com.example.attendance.clock.repository.TimeRecordRepository;
import com.example.attendance.clock.service.WorkTimeCalculator;
import com.example.attendance.common.enums.ApplicationStatus;
import com.example.attendance.common.enums.ApplicationType;
import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.notification.service.NotificationService;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TimeRecordRepository timeRecordRepository;
    @Mock
    private CompanyCalendarRepository companyCalendarRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaidLeaveService paidLeaveService;

    private ApplicationServiceImpl service;

    private UUID userId;
    private UUID approverId;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        service = new ApplicationServiceImpl(
                applicationRepository, userRepository, timeRecordRepository,
                companyCalendarRepository, notificationService, paidLeaveService);
        userId = UUID.randomUUID();
        approverId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
    }

    private User createUser(UUID id, Role role, UUID deptId) {
        return User.builder()
                .email(id + "@example.com")
                .passwordHash("hashed")
                .name("ユーザ " + id.toString().substring(0, 4))
                .role(role)
                .primaryDepartmentId(deptId)
                .active(true)
                .failedLoginAttempts(0)
                .requirePasswordChange(false)
                .build();
    }

    @Test
    @DisplayName("打刻修正申請: 正常に提出され PENDING 状態で返される")
    void submitClockFix_valid_returnsPending() {
        var user = createUser(userId, Role.GENERAL, departmentId);
        var approver = createUser(approverId, Role.APPROVER, departmentId);
        var request = new ClockFixRequest(
                LocalDate.now().minusDays(1),
                ZonedDateTime.now(TOKYO).withHour(9).withMinute(0),
                ZonedDateTime.now(TOKYO).withHour(18).withMinute(0),
                "打刻忘れのため");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyCalendarRepository.existsByHolidayDate(any(LocalDate.class))).thenReturn(false);
        when(userRepository.findByPrimaryDepartmentIdAndActiveOrderByNameAsc(departmentId, true))
                .thenReturn(List.of(user, approver));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        var result = service.submitClockFix(userId, request);

        assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(result.type()).isEqualTo(ApplicationType.CLOCK_FIX);
        assertThat(result.clockFixDetail().targetDate()).isEqualTo(request.targetDate());
        assertThat(result.clockFixDetail().reason()).isEqualTo("打刻忘れのため");
    }

    @Test
    @DisplayName("打刻修正申請: 遡及期限を過ぎた日付は申請不可")
    void submitClockFix_pastDeadline_throwsBusinessException() {
        var user = createUser(userId, Role.GENERAL, departmentId);
        var targetDate = LocalDate.now().minusMonths(2);
        var request = new ClockFixRequest(
                targetDate,
                ZonedDateTime.now(TOKYO).withHour(9).withMinute(0),
                ZonedDateTime.now(TOKYO).withHour(18).withMinute(0),
                "打刻忘れ");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyCalendarRepository.existsByHolidayDate(any(LocalDate.class))).thenReturn(false);

        assertThatThrownBy(() -> service.submitClockFix(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("遡及期限");
    }

    @Test
    @DisplayName("承認: PENDING の申請を承認すると APPROVED になる")
    void approve_pendingApplication_statusBecomesApproved() {
        var application = new Application(userId, ApplicationType.CLOCK_FIX);
        var detail = new ClockFixDetail(application, LocalDate.now().minusDays(1),
                ZonedDateTime.now(TOKYO).withHour(9).withMinute(0),
                ZonedDateTime.now(TOKYO).withHour(18).withMinute(0),
                "理由");
        application.setClockFixDetail(detail);

        var approverUser = createUser(approverId, Role.APPROVER, departmentId);
        var applicantUser = createUser(userId, Role.GENERAL, departmentId);
        var timeRecord = new TimeRecord();

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(applicantUser));
        when(timeRecordRepository.findByUserIdAndWorkDate(userId, detail.getTargetDate()))
                .thenReturn(Optional.of(timeRecord));
        when(timeRecordRepository.save(any())).thenReturn(timeRecord);
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        var result = service.approve(application.getId(), approverId);

        assertThat(result.status()).isEqualTo(ApplicationStatus.APPROVED);
        verify(notificationService).send(eq(userId), eq(NotificationType.APPLICATION_APPROVED),
                any(), any(), eq(application.getId()));
    }

    @Test
    @DisplayName("承認: 自分自身の申請は自己承認できない")
    void approve_selfApplication_throwsBusinessException() {
        var application = new Application(approverId, ApplicationType.CLOCK_FIX);
        var detail = new ClockFixDetail(application, LocalDate.now().minusDays(1),
                ZonedDateTime.now(TOKYO).withHour(9).withMinute(0),
                ZonedDateTime.now(TOKYO).withHour(18).withMinute(0),
                "理由");
        application.setClockFixDetail(detail);

        var approverUser = createUser(approverId, Role.APPROVER, departmentId);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));

        assertThatThrownBy(() -> service.approve(application.getId(), approverId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("自分自身の申請");
    }

    @Test
    @DisplayName("承認: 他部署の承認者(APPROVER)は承認できない")
    void approve_differentDepartmentApprover_throwsBusinessException() {
        var otherDepartmentId = UUID.randomUUID();
        var application = new Application(userId, ApplicationType.CLOCK_FIX);
        var detail = new ClockFixDetail(application, LocalDate.now().minusDays(1),
                ZonedDateTime.now(TOKYO).withHour(9).withMinute(0),
                ZonedDateTime.now(TOKYO).withHour(18).withMinute(0),
                "理由");
        application.setClockFixDetail(detail);

        var approverUser = createUser(approverId, Role.APPROVER, otherDepartmentId);
        var applicantUser = createUser(userId, Role.GENERAL, departmentId);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(applicantUser));

        assertThatThrownBy(() -> service.approve(application.getId(), approverId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("他部署");
    }

    @Test
    @DisplayName("承認: ADMINは部署が異なっても承認できる")
    void approve_adminDifferentDepartment_statusBecomesApproved() {
        var otherDepartmentId = UUID.randomUUID();
        var application = new Application(userId, ApplicationType.CLOCK_FIX);
        var detail = new ClockFixDetail(application, LocalDate.now().minusDays(1),
                ZonedDateTime.now(TOKYO).withHour(9).withMinute(0),
                ZonedDateTime.now(TOKYO).withHour(18).withMinute(0),
                "理由");
        application.setClockFixDetail(detail);

        var adminUser = createUser(approverId, Role.ADMIN, otherDepartmentId);
        var applicantUser = createUser(userId, Role.GENERAL, departmentId);
        var timeRecord = new TimeRecord();

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(userRepository.findById(approverId)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(applicantUser));
        when(timeRecordRepository.findByUserIdAndWorkDate(userId, detail.getTargetDate()))
                .thenReturn(Optional.of(timeRecord));
        when(timeRecordRepository.save(any())).thenReturn(timeRecord);
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        var result = service.approve(application.getId(), approverId);

        assertThat(result.status()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    @DisplayName("却下: PENDING の申請を却下すると REJECTED になる")
    void reject_pendingApplication_statusBecomesRejected() {
        var application = new Application(userId, ApplicationType.CLOCK_FIX);
        var detail = new ClockFixDetail(application, LocalDate.now().minusDays(1),
                null, null, "理由");
        application.setClockFixDetail(detail);

        var approverUser = createUser(approverId, Role.APPROVER, departmentId);
        var applicantUser = createUser(userId, Role.GENERAL, departmentId);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approverUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(applicantUser));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        var result = service.reject(application.getId(), approverId, "理由不十分");

        assertThat(result.status()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(result.rejectionComment()).isEqualTo("理由不十分");
        verify(notificationService).send(eq(userId), eq(NotificationType.APPLICATION_REJECTED),
                any(), any(), eq(application.getId()));
    }

    @Test
    @DisplayName("取り下げ: PENDING の申請のみ取り下げ可能")
    void withdraw_pendingApplication_statusBecomesWithdrawn() {
        var application = new Application(userId, ApplicationType.CLOCK_FIX);
        var detail = new ClockFixDetail(application, LocalDate.now(), null, null, "理由");
        application.setClockFixDetail(detail);

        var applicantUser = createUser(userId, Role.GENERAL, departmentId);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(userRepository.findById(userId)).thenReturn(Optional.of(applicantUser));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        var result = service.withdraw(application.getId(), userId);

        assertThat(result.status()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("取り下げ: APPROVED の申請は取り下げ不可")
    void withdraw_approvedApplication_throwsBusinessException() {
        var application = new Application(userId, ApplicationType.CLOCK_FIX);
        application.approve(approverId);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> service.withdraw(application.getId(), userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("取り下げ: 他人の申請は取り下げ不可")
    void withdraw_otherUsersApplication_throwsBusinessException() {
        var application = new Application(userId, ApplicationType.CLOCK_FIX);
        var otherUserId = UUID.randomUUID();

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> service.withdraw(application.getId(), otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("自分の申請");
    }

    @Test
    @DisplayName("申請一覧: ユーザの申請が返される")
    void getMyApplications_returnsUserApplications() {
        var application = new Application(userId, ApplicationType.CLOCK_FIX);
        var detail = new ClockFixDetail(application, LocalDate.now(), null, null, "理由");
        application.setClockFixDetail(detail);

        var applicantUser = createUser(userId, Role.GENERAL, departmentId);
        var page = new PageImpl<>(List.of(application));

        when(userRepository.findById(userId)).thenReturn(Optional.of(applicantUser));
        when(applicationRepository.findByApplicantIdOrderByAppliedAtDesc(eq(userId), any()))
                .thenReturn(page);

        var result = service.getMyApplications(userId, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("承認待ち一覧: 承認権限のないGENERALユーザは取得できない")
    void getPendingApplications_generalRole_throwsBusinessException() {
        var generalUser = createUser(userId, Role.GENERAL, departmentId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(generalUser));

        assertThatThrownBy(() -> service.getPendingApplications(userId, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("承認権限");
    }
}
