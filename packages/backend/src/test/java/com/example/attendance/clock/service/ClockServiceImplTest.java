package com.example.attendance.clock.service;

import com.example.attendance.clock.entity.TimeRecord;
import com.example.attendance.clock.repository.TimeEntryRepository;
import com.example.attendance.clock.repository.TimeRecordRepository;
import com.example.attendance.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClockServiceImplTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private CalendarService calendarService;

    private ClockServiceImpl clockService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    @BeforeEach
    void setUp() {
        clockService = new ClockServiceImpl(timeRecordRepository, timeEntryRepository, calendarService);
    }

    @Test
    @DisplayName("出勤打刻: 当日未打刻の場合、正常に出勤レコードが作成される")
    void clockIn_notYetClockedIn_createsRecord() {
        var today = LocalDate.now(TOKYO);
        when(timeRecordRepository.findByUserIdAndWorkDate(USER_ID, today))
                .thenReturn(Optional.empty());
        when(calendarService.isHoliday(today)).thenReturn(false);
        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = clockService.clockIn(USER_ID);

        assertThat(result.workDate()).isEqualTo(today);
        assertThat(result.clockIn()).isNotNull();
        assertThat(result.clockOut()).isNull();
    }

    @Test
    @DisplayName("出勤打刻: 既に打刻済みの場合、409エラー")
    void clockIn_alreadyClockedIn_throwsConflict() {
        var today = LocalDate.now(TOKYO);
        var existing = TimeRecord.builder()
                .userId(USER_ID)
                .workDate(today)
                .clockIn(ZonedDateTime.now(TOKYO))
                .timezone(TOKYO.getId())
                .build();
        when(timeRecordRepository.findByUserIdAndWorkDate(USER_ID, today))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> clockService.clockIn(USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("既に出勤打刻済み");
    }

    @Test
    @DisplayName("退勤打刻: 出勤済みの場合、勤務時間が計算される")
    void clockOut_afterClockIn_calculatesWorkTime() {
        var today = LocalDate.now(TOKYO);
        var clockInTime = ZonedDateTime.now(TOKYO).minusHours(8);
        var record = TimeRecord.builder()
                .userId(USER_ID)
                .workDate(today)
                .clockIn(clockInTime)
                .breakMinutes(60)
                .isHoliday(false)
                .timezone(TOKYO.getId())
                .build();
        when(timeRecordRepository.findByUserIdAndWorkDate(USER_ID, today))
                .thenReturn(Optional.of(record));
        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = clockService.clockOut(USER_ID);

        assertThat(result.clockOut()).isNotNull();
        assertThat(result.workMinutes()).isNotNull();
        assertThat(result.workMinutes()).isGreaterThan(0);
    }

    @Test
    @DisplayName("退勤打刻: 出勤打刻がない場合、400エラー")
    void clockOut_noClockIn_throwsBadRequest() {
        var today = LocalDate.now(TOKYO);
        when(timeRecordRepository.findByUserIdAndWorkDate(USER_ID, today))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clockService.clockOut(USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("出勤打刻がありません");
    }

    @Test
    @DisplayName("退勤打刻: 既に退勤済みの場合、409エラー")
    void clockOut_alreadyClockedOut_throwsConflict() {
        var today = LocalDate.now(TOKYO);
        var record = TimeRecord.builder()
                .userId(USER_ID)
                .workDate(today)
                .clockIn(ZonedDateTime.now(TOKYO).minusHours(8))
                .clockOut(ZonedDateTime.now(TOKYO))
                .timezone(TOKYO.getId())
                .build();
        when(timeRecordRepository.findByUserIdAndWorkDate(USER_ID, today))
                .thenReturn(Optional.of(record));

        assertThatThrownBy(() -> clockService.clockOut(USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("既に退勤打刻済み");
    }

    @Test
    @DisplayName("打刻状態取得: 未出勤の場合、NOT_CLOCKED_IN")
    void getStatus_notClockedIn_returnsNotClockedInState() {
        var today = LocalDate.now(TOKYO);
        when(timeRecordRepository.findByUserIdAndWorkDate(USER_ID, today))
                .thenReturn(Optional.empty());

        var result = clockService.getStatus(USER_ID);

        assertThat(result.currentState()).isEqualTo("NOT_CLOCKED_IN");
        assertThat(result.clockIn()).isNull();
    }

    @Test
    @DisplayName("打刻状態取得: 出勤済み・退勤前の場合、WORKING")
    void getStatus_clockedIn_returnsWorkingState() {
        var today = LocalDate.now(TOKYO);
        var record = TimeRecord.builder()
                .userId(USER_ID)
                .workDate(today)
                .clockIn(ZonedDateTime.now(TOKYO))
                .timezone(TOKYO.getId())
                .build();
        when(timeRecordRepository.findByUserIdAndWorkDate(USER_ID, today))
                .thenReturn(Optional.of(record));
        when(timeEntryRepository.findByTimeRecordIdOrderByRecordedAtAsc(record.getId()))
                .thenReturn(List.of());

        var result = clockService.getStatus(USER_ID);

        assertThat(result.currentState()).isEqualTo("WORKING");
    }

    @Test
    @DisplayName("月次集計: レコードがある場合、集計値が正しい")
    void getMonthlySummary_withRecords_returnsSummary() {
        var record = TimeRecord.builder()
                .userId(USER_ID)
                .workDate(LocalDate.of(2026, 7, 1))
                .clockIn(ZonedDateTime.of(2026, 7, 1, 9, 0, 0, 0, TOKYO))
                .clockOut(ZonedDateTime.of(2026, 7, 1, 18, 0, 0, 0, TOKYO))
                .workMinutes(480)
                .overtimeMinutes(30)
                .nightMinutes(0)
                .holidayWorkMinutes(0)
                .timezone(TOKYO.getId())
                .build();
        when(timeRecordRepository.findByUserIdAndYearMonth(any(), any(), any()))
                .thenReturn(List.of(record));

        var result = clockService.getMonthlySummary(USER_ID, 2026, 7);

        assertThat(result.workDays()).isEqualTo(1);
        assertThat(result.totalWorkMinutes()).isEqualTo(480);
        assertThat(result.totalOvertimeMinutes()).isEqualTo(30);
    }
}
