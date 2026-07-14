package com.example.attendance.application.service;

import com.example.attendance.application.dto.ApplicationResponse;
import com.example.attendance.application.dto.ClockFixRequest;
import com.example.attendance.application.dto.LeaveRequest;
import com.example.attendance.application.entity.Application;
import com.example.attendance.application.entity.ClockFixDetail;
import com.example.attendance.application.entity.LeaveRequestDetail;
import com.example.attendance.application.repository.ApplicationRepository;
import com.example.attendance.clock.repository.CompanyCalendarRepository;
import com.example.attendance.clock.repository.TimeRecordRepository;
import com.example.attendance.clock.service.WorkTimeCalculator;
import com.example.attendance.common.enums.ApplicationStatus;
import com.example.attendance.common.enums.ApplicationType;
import com.example.attendance.common.enums.LeaveType;
import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.notification.service.NotificationService;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final TimeRecordRepository timeRecordRepository;
    private final CompanyCalendarRepository companyCalendarRepository;
    private final NotificationService notificationService;
    private final PaidLeaveService paidLeaveService;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  UserRepository userRepository,
                                  TimeRecordRepository timeRecordRepository,
                                  CompanyCalendarRepository companyCalendarRepository,
                                  NotificationService notificationService,
                                  PaidLeaveService paidLeaveService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.timeRecordRepository = timeRecordRepository;
        this.companyCalendarRepository = companyCalendarRepository;
        this.notificationService = notificationService;
        this.paidLeaveService = paidLeaveService;
    }

    @Override
    @Transactional
    public ApplicationResponse submitClockFix(UUID userId, ClockFixRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("ユーザが見つかりません", HttpStatus.NOT_FOUND));

        validateDeadline(request.targetDate());

        var application = new Application(userId, ApplicationType.CLOCK_FIX);
        var detail = new ClockFixDetail(application, request.targetDate(),
                request.correctedClockIn(), request.correctedClockOut(), request.reason());
        application.setClockFixDetail(detail);

        applicationRepository.save(application);

        notifyApprovers(user, application);

        return ApplicationResponse.from(application, user.getName());
    }

    @Override
    @Transactional
    public ApplicationResponse submitLeaveRequest(UUID userId, LeaveRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("ユーザが見つかりません", HttpStatus.NOT_FOUND));

        var leaveType = LeaveType.valueOf(request.leaveType());

        if (!paidLeaveService.canApply(userId, leaveType)) {
            throw new BusinessException("有給残日数が不足しています", HttpStatus.BAD_REQUEST);
        }

        var application = new Application(userId, ApplicationType.LEAVE_REQUEST);
        var detail = new LeaveRequestDetail(application, request.leaveDate(), leaveType);
        application.setLeaveRequestDetail(detail);

        applicationRepository.save(application);

        notifyApprovers(user, application);

        return ApplicationResponse.from(application, user.getName());
    }

    @Override
    @Transactional
    public ApplicationResponse approve(UUID applicationId, UUID approverId) {
        var application = findApplication(applicationId);
        validatePending(application);

        var approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException("承認者が見つかりません", HttpStatus.NOT_FOUND));
        validateApproverRole(approver);

        application.approve(approverId);

        if (application.getType() == ApplicationType.CLOCK_FIX) {
            applyClockFix(application);
        } else if (application.getType() == ApplicationType.LEAVE_REQUEST) {
            applyLeaveRequest(application);
        }

        applicationRepository.save(application);

        var applicantName = getUserName(application.getApplicantId());
        notificationService.send(application.getApplicantId(), NotificationType.APPLICATION_APPROVED,
                "申請が承認されました",
                approver.getName() + "さんがあなたの申請を承認しました",
                applicationId);

        return ApplicationResponse.from(application, applicantName);
    }

    @Override
    @Transactional
    public ApplicationResponse reject(UUID applicationId, UUID approverId, String comment) {
        var application = findApplication(applicationId);
        validatePending(application);

        var approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException("承認者が見つかりません", HttpStatus.NOT_FOUND));
        validateApproverRole(approver);

        application.reject(approverId, comment);
        applicationRepository.save(application);

        var applicantName = getUserName(application.getApplicantId());
        notificationService.send(application.getApplicantId(), NotificationType.APPLICATION_REJECTED,
                "申請が却下されました",
                approver.getName() + "さんがあなたの申請を却下しました: " + comment,
                applicationId);

        return ApplicationResponse.from(application, applicantName);
    }

    @Override
    @Transactional
    public ApplicationResponse withdraw(UUID applicationId, UUID userId) {
        var application = findApplication(applicationId);

        if (!application.getApplicantId().equals(userId)) {
            throw new BusinessException("自分の申請のみ取り下げ可能です", HttpStatus.FORBIDDEN);
        }
        validatePending(application);

        application.withdraw();
        applicationRepository.save(application);

        var applicantName = getUserName(userId);
        return ApplicationResponse.from(application, applicantName);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(UUID userId, String status, Pageable pageable) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("ユーザが見つかりません", HttpStatus.NOT_FOUND));

        Page<Application> page;
        if (status != null && !status.isBlank()) {
            var appStatus = ApplicationStatus.valueOf(status.toUpperCase());
            page = applicationRepository.findByApplicantIdAndStatusOrderByAppliedAtDesc(userId, appStatus, pageable);
        } else {
            page = applicationRepository.findByApplicantIdOrderByAppliedAtDesc(userId, pageable);
        }

        return page.map(app -> ApplicationResponse.from(app, user.getName()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getPendingApplications(UUID approverId, Pageable pageable) {
        var approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException("承認者が見つかりません", HttpStatus.NOT_FOUND));

        var page = applicationRepository.findPendingByDepartment(approver.getPrimaryDepartmentId(), pageable);

        List<UUID> applicantIds = page.getContent().stream()
                .map(Application::getApplicantId)
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, String> nameMap = applicationRepository.findUserNamesByIds(applicantIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (String) row[1]
                ));

        return page.map(app -> ApplicationResponse.from(app, nameMap.getOrDefault(app.getApplicantId(), "不明")));
    }

    private void validateDeadline(LocalDate targetDate) {
        var targetMonth = targetDate.withDayOfMonth(1);
        var deadlineBase = targetMonth.plusMonths(1);
        var deadline = calculateThirdBusinessDay(deadlineBase);

        if (LocalDate.now().isAfter(deadline)) {
            throw new BusinessException(
                    "遡及期限（翌月第3営業日）を超えています。対象日: " + targetDate, HttpStatus.BAD_REQUEST);
        }
    }

    private LocalDate calculateThirdBusinessDay(LocalDate monthStart) {
        int businessDays = 0;
        var current = monthStart;
        while (businessDays < 3) {
            if (isBusinessDay(current)) {
                businessDays++;
            }
            if (businessDays < 3) {
                current = current.plusDays(1);
            }
        }
        return current;
    }

    private boolean isBusinessDay(LocalDate date) {
        var dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return false;
        }
        return !companyCalendarRepository.existsByHolidayDate(date);
    }

    private void applyClockFix(Application application) {
        var detail = application.getClockFixDetail();
        var timeRecord = timeRecordRepository.findByUserIdAndWorkDate(
                application.getApplicantId(), detail.getTargetDate()).orElse(null);

        if (timeRecord == null) {
            return;
        }

        if (detail.getCorrectedClockIn() != null) {
            timeRecord.setClockIn(detail.getCorrectedClockIn());
        }
        if (detail.getCorrectedClockOut() != null) {
            timeRecord.setClockOut(detail.getCorrectedClockOut());
        }

        if (timeRecord.getClockIn() != null && timeRecord.getClockOut() != null) {
            var result = WorkTimeCalculator.calculate(
                    timeRecord.getClockIn(), timeRecord.getClockOut(),
                    timeRecord.getBreakMinutes(), timeRecord.getIsHoliday());
            timeRecord.setWorkMinutes(result.workMinutes());
            timeRecord.setOvertimeMinutes(result.overtimeMinutes());
            timeRecord.setNightMinutes(result.nightMinutes());
            timeRecord.setHolidayWorkMinutes(result.holidayWorkMinutes());
        }

        timeRecordRepository.save(timeRecord);
    }

    private void applyLeaveRequest(Application application) {
        var detail = application.getLeaveRequestDetail();
        if (detail != null) {
            paidLeaveService.consume(application.getApplicantId(), detail.getLeaveType());
        }
    }

    private void notifyApprovers(User applicant, Application application) {
        var deptMembers = userRepository.findByPrimaryDepartmentIdAndActiveOrderByNameAsc(
                applicant.getPrimaryDepartmentId(), true);

        deptMembers.stream()
                .filter(u -> u.getRole() == Role.APPROVER || u.getRole() == Role.ADMIN)
                .forEach(approver -> notificationService.send(
                        approver.getId(),
                        NotificationType.APPLICATION_RECEIVED,
                        "新しい申請が届きました",
                        applicant.getName() + "さんから打刻修正申請が届きました",
                        application.getId()));
    }

    private void validateApproverRole(User user) {
        if (user.getRole() != Role.APPROVER && user.getRole() != Role.ADMIN) {
            throw new BusinessException("承認権限がありません", HttpStatus.FORBIDDEN);
        }
    }

    private void validatePending(Application application) {
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException(
                    "PENDINGステータスの申請のみ操作可能です（現在: " + application.getStatus() + "）", HttpStatus.BAD_REQUEST);
        }
    }

    private Application findApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("申請が見つかりません", HttpStatus.NOT_FOUND));
    }

    private String getUserName(UUID userId) {
        return userRepository.findById(userId).map(User::getName).orElse("不明");
    }
}
