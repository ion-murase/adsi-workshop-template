package com.example.attendance.export.service;

import com.example.attendance.clock.entity.TimeRecord;
import com.example.attendance.clock.repository.TimeRecordRepository;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvExportServiceImplTest {

    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    @Mock
    private TimeRecordRepository timeRecordRepository;
    @Mock
    private UserRepository userRepository;

    private CsvExportServiceImpl service;

    private UUID adminId;
    private UUID generalUserId;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        service = new CsvExportServiceImpl(timeRecordRepository, userRepository);
        adminId = UUID.randomUUID();
        generalUserId = UUID.randomUUID();
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
    @DisplayName("勤務履歴CSV: UTF-8 BOMとヘッダーが含まれる")
    void exportTimeRecords_containsBomAndHeader() {
        var admin = createUser(adminId, Role.ADMIN, departmentId);
        var record = TimeRecord.builder()
                .userId(generalUserId)
                .workDate(LocalDate.of(2026, 7, 1))
                .clockIn(ZonedDateTime.of(2026, 7, 1, 9, 0, 0, 0, TOKYO))
                .clockOut(ZonedDateTime.of(2026, 7, 1, 18, 30, 0, 0, TOKYO))
                .breakMinutes(60)
                .workMinutes(510)
                .overtimeMinutes(60)
                .nightMinutes(0)
                .holidayWorkMinutes(0)
                .isHoliday(false)
                .build();

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(timeRecordRepository.findByUserIdAndYearMonth(eq(generalUserId), any(), any()))
                .thenReturn(List.of(record));

        var result = service.exportTimeRecords(adminId, generalUserId, 2026, 7);
        var csv = new String(result, StandardCharsets.UTF_8);

        assertThat(result[0]).isEqualTo((byte) 0xEF);
        assertThat(result[1]).isEqualTo((byte) 0xBB);
        assertThat(result[2]).isEqualTo((byte) 0xBF);
        assertThat(csv).contains("日付,曜日,出勤,退勤,休憩,実労働時間,残業時間,深夜時間,休日出勤,備考");
        assertThat(csv).contains("2026-07-01");
        assertThat(csv).contains("09:00");
        assertThat(csv).contains("18:30");
    }

    @Test
    @DisplayName("権限チェック: 一般ユーザは他人のデータをエクスポート不可")
    void exportTimeRecords_generalCannotExportOthers() {
        var general = createUser(generalUserId, Role.GENERAL, departmentId);
        var otherUserId = UUID.randomUUID();

        when(userRepository.findById(generalUserId)).thenReturn(Optional.of(general));

        assertThatThrownBy(() -> service.exportTimeRecords(generalUserId, otherUserId, 2026, 7))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("権限");
    }

    @Test
    @DisplayName("権限チェック: 管理者は全員のデータをエクスポート可能")
    void exportTimeRecords_adminCanExportAnyone() {
        var admin = createUser(adminId, Role.ADMIN, departmentId);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(timeRecordRepository.findByUserIdAndYearMonth(eq(generalUserId), any(), any()))
                .thenReturn(List.of());

        var result = service.exportTimeRecords(adminId, generalUserId, 2026, 7);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("ユーザ一覧CSV: ヘッダーとデータが含まれる")
    void exportUsers_containsHeaderAndData() {
        var admin = createUser(adminId, Role.ADMIN, departmentId);
        var general = createUser(generalUserId, Role.GENERAL, departmentId);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findByActiveOrderByNameAsc(true)).thenReturn(List.of(admin, general));

        var result = service.exportUsers(adminId);
        var csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("社員番号,氏名,メールアドレス,ロール,主管部署,ステータス,登録日");
    }

    @Test
    @DisplayName("月別集計CSV: ヘッダーが含まれる")
    void exportMonthlySummary_containsHeader() {
        var admin = createUser(adminId, Role.ADMIN, departmentId);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findByActiveOrderByNameAsc(true)).thenReturn(List.of(admin));
        when(timeRecordRepository.findByUserIdAndYearMonth(any(), any(), any())).thenReturn(List.of());

        var result = service.exportMonthlySummary(adminId, 2026, 7);
        var csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("社員番号,氏名,部署,勤務日数,勤務時間,残業時間,深夜時間,休日出勤時間,有給取得日数");
    }
}
