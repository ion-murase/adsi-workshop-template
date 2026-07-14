package com.example.attendance.application.service;

import com.example.attendance.application.entity.PaidLeaveBalance;
import com.example.attendance.application.repository.PaidLeaveBalanceRepository;
import com.example.attendance.common.enums.LeaveType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.export.dto.PaidLeaveBalanceResponse;
import com.example.attendance.organization.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
public class PaidLeaveServiceImpl implements PaidLeaveService {

    private static final BigDecimal HALF_DAY = new BigDecimal("0.5");
    private static final BigDecimal FULL_DAY = new BigDecimal("1.0");

    private final PaidLeaveBalanceRepository balanceRepository;
    private final UserRepository userRepository;

    public PaidLeaveServiceImpl(PaidLeaveBalanceRepository balanceRepository,
                                UserRepository userRepository) {
        this.balanceRepository = balanceRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaidLeaveBalanceResponse getBalance(UUID userId) {
        int currentFiscalYear = getCurrentFiscalYear();
        var balance = balanceRepository.findByUserIdAndFiscalYear(userId, currentFiscalYear)
                .orElse(new PaidLeaveBalance(userId, currentFiscalYear, BigDecimal.ZERO, BigDecimal.ZERO,
                        LocalDate.of(currentFiscalYear + 1, 3, 31)));

        return new PaidLeaveBalanceResponse(
                balance.getFiscalYear(),
                balance.getGrantedDays(),
                balance.getUsedDays(),
                balance.getCarriedOverDays(),
                balance.getRemainingDays()
        );
    }

    @Override
    @Transactional
    public void grantAnnualLeave(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("ユーザが見つかりません", HttpStatus.NOT_FOUND));

        var serviceYears = calculateServiceYears(user.getCreatedAt());
        var grantDays = calculateGrantDays(serviceYears);
        var currentFiscalYear = getCurrentFiscalYear();

        var carriedOver = balanceRepository.findTopByUserIdOrderByFiscalYearDesc(userId)
                .map(PaidLeaveBalance::getRemainingDays)
                .orElse(BigDecimal.ZERO);

        var expiresAt = LocalDate.of(currentFiscalYear + 1, 3, 31);
        var balance = new PaidLeaveBalance(userId, currentFiscalYear, grantDays, carriedOver, expiresAt);
        balanceRepository.save(balance);

        log.info("Annual leave granted: user={}, days={}, carriedOver={}", userId, grantDays, carriedOver);
    }

    @Override
    @Transactional
    public void consume(UUID userId, LeaveType leaveType) {
        int currentFiscalYear = getCurrentFiscalYear();
        var balance = balanceRepository.findByUserIdAndFiscalYear(userId, currentFiscalYear)
                .orElseThrow(() -> new BusinessException("有給残日数が見つかりません", HttpStatus.NOT_FOUND));

        var days = leaveType == LeaveType.FULL_DAY ? FULL_DAY : HALF_DAY;
        balance.consume(days);
        balanceRepository.save(balance);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canApply(UUID userId, LeaveType leaveType) {
        int currentFiscalYear = getCurrentFiscalYear();
        var balance = balanceRepository.findByUserIdAndFiscalYear(userId, currentFiscalYear)
                .orElse(null);

        if (balance == null) {
            return false;
        }

        var required = leaveType == LeaveType.FULL_DAY ? FULL_DAY : HALF_DAY;
        return balance.getRemainingDays().compareTo(required) >= 0;
    }

    private int calculateServiceYears(ZonedDateTime hireDate) {
        var months = ChronoUnit.MONTHS.between(hireDate.toLocalDate().withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(1));
        return (int) (months / 12);
    }

    private BigDecimal calculateGrantDays(int serviceYears) {
        if (serviceYears >= 6) return new BigDecimal("20");
        if (serviceYears >= 5) return new BigDecimal("18");
        if (serviceYears >= 4) return new BigDecimal("16");
        if (serviceYears >= 3) return new BigDecimal("14");
        if (serviceYears >= 2) return new BigDecimal("12");
        if (serviceYears >= 1) return new BigDecimal("11");
        return new BigDecimal("10");
    }

    private int getCurrentFiscalYear() {
        var now = LocalDate.now();
        return now.getMonthValue() >= 4 ? now.getYear() : now.getYear() - 1;
    }
}
