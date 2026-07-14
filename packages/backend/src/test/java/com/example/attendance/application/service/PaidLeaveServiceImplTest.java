package com.example.attendance.application.service;

import com.example.attendance.application.entity.PaidLeaveBalance;
import com.example.attendance.application.repository.PaidLeaveBalanceRepository;
import com.example.attendance.common.enums.LeaveType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaidLeaveServiceImplTest {

    @Mock
    private PaidLeaveBalanceRepository balanceRepository;
    @Mock
    private UserRepository userRepository;

    private PaidLeaveServiceImpl service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new PaidLeaveServiceImpl(balanceRepository, userRepository);
        userId = UUID.randomUUID();
    }

    private User createUserWithCreatedAt(ZonedDateTime createdAt) {
        var user = User.builder()
                .email("test@example.com")
                .passwordHash("hashed")
                .name("テストユーザ")
                .role(Role.GENERAL)
                .primaryDepartmentId(UUID.randomUUID())
                .active(true)
                .failedLoginAttempts(0)
                .requirePasswordChange(false)
                .build();
        // BaseEntity の createdAt はコンストラクタで設定できないのでリフレクションで設定
        try {
            var field = user.getClass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(user, createdAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @ParameterizedTest
    @DisplayName("付与計算: 勤続年数に応じた付与日数が正しい")
    @CsvSource({
            "0, 10",
            "1, 11",
            "2, 12",
            "3, 14",
            "4, 16",
            "5, 18",
            "6, 20",
            "10, 20"
    })
    void grantAnnualLeave_byServiceYears_grantsCorrectDays(int yearsAgo, int expectedDays) {
        var createdAt = ZonedDateTime.now().minusYears(yearsAgo).minusMonths(6);
        var user = createUserWithCreatedAt(createdAt);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(balanceRepository.findTopByUserIdOrderByFiscalYearDesc(userId)).thenReturn(Optional.empty());
        when(balanceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.grantAnnualLeave(userId);

        var captor = ArgumentCaptor.forClass(PaidLeaveBalance.class);
        verify(balanceRepository).save(captor.capture());
        assertThat(captor.getValue().getGrantedDays()).isEqualByComparingTo(new BigDecimal(expectedDays));
    }

    @Test
    @DisplayName("消化: FULL_DAYで1.0日消化される")
    void consume_fullDay_deductsOneDay() {
        var balance = new PaidLeaveBalance(userId, 2026, new BigDecimal("10"), BigDecimal.ZERO,
                LocalDate.of(2027, 3, 31));

        when(balanceRepository.findByUserIdAndFiscalYear(any(), any())).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.consume(userId, LeaveType.FULL_DAY);

        assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(balance.getRemainingDays()).isEqualByComparingTo(new BigDecimal("9.0"));
    }

    @Test
    @DisplayName("消化: HALF_AMで0.5日消化される")
    void consume_halfDay_deductsHalfDay() {
        var balance = new PaidLeaveBalance(userId, 2026, new BigDecimal("10"), BigDecimal.ZERO,
                LocalDate.of(2027, 3, 31));

        when(balanceRepository.findByUserIdAndFiscalYear(any(), any())).thenReturn(Optional.of(balance));
        when(balanceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.consume(userId, LeaveType.HALF_AM);

        assertThat(balance.getUsedDays()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    @Test
    @DisplayName("残日数不足: 残日数ゼロで申請不可")
    void canApply_noRemainingDays_returnsFalse() {
        var balance = new PaidLeaveBalance(userId, 2026, new BigDecimal("10"), BigDecimal.ZERO,
                LocalDate.of(2027, 3, 31));
        balance.consume(new BigDecimal("10"));

        when(balanceRepository.findByUserIdAndFiscalYear(any(), any())).thenReturn(Optional.of(balance));

        var result = service.canApply(userId, LeaveType.FULL_DAY);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("残日数あり: 残日数十分で申請可能")
    void canApply_hasRemainingDays_returnsTrue() {
        var balance = new PaidLeaveBalance(userId, 2026, new BigDecimal("10"), BigDecimal.ZERO,
                LocalDate.of(2027, 3, 31));

        when(balanceRepository.findByUserIdAndFiscalYear(any(), any())).thenReturn(Optional.of(balance));

        var result = service.canApply(userId, LeaveType.FULL_DAY);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("繰越: 前年度未消化分が繰り越される")
    void grantAnnualLeave_withPreviousBalance_carriesOver() {
        var createdAt = ZonedDateTime.now().minusYears(2).minusMonths(6);
        var user = createUserWithCreatedAt(createdAt);
        var previousBalance = new PaidLeaveBalance(userId, 2025, new BigDecimal("11"), BigDecimal.ZERO,
                LocalDate.of(2027, 3, 31));
        previousBalance.consume(new BigDecimal("5"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(balanceRepository.findTopByUserIdOrderByFiscalYearDesc(userId))
                .thenReturn(Optional.of(previousBalance));
        when(balanceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.grantAnnualLeave(userId);

        var captor = ArgumentCaptor.forClass(PaidLeaveBalance.class);
        verify(balanceRepository).save(captor.capture());
        assertThat(captor.getValue().getCarriedOverDays()).isEqualByComparingTo(new BigDecimal("6.0"));
    }

    @Test
    @DisplayName("残日数取得: 正しいレスポンスが返される")
    void getBalance_returnsCorrectResponse() {
        var balance = new PaidLeaveBalance(userId, 2026, new BigDecimal("12"), new BigDecimal("3"),
                LocalDate.of(2027, 3, 31));
        balance.consume(new BigDecimal("5"));

        when(balanceRepository.findByUserIdAndFiscalYear(any(), any())).thenReturn(Optional.of(balance));

        var result = service.getBalance(userId);

        assertThat(result.fiscalYear()).isEqualTo(2026);
        assertThat(result.grantedDays()).isEqualByComparingTo(new BigDecimal("12"));
        assertThat(result.usedDays()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(result.carriedOverDays()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(result.remainingDays()).isEqualByComparingTo(new BigDecimal("10"));
    }
}
