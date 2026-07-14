package com.example.attendance.clock.service;

import com.example.attendance.clock.entity.CompanyCalendar;
import com.example.attendance.clock.repository.CompanyCalendarRepository;
import com.example.attendance.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceImplTest {

    @Mock
    private CompanyCalendarRepository calendarRepository;

    private CalendarServiceImpl calendarService;

    @BeforeEach
    void setUp() {
        calendarService = new CalendarServiceImpl(calendarRepository);
    }

    @Test
    @DisplayName("休日判定: 土曜日はtrue")
    void isHoliday_saturday_returnsTrue() {
        var saturday = LocalDate.of(2026, 7, 18);
        assertThat(calendarService.isHoliday(saturday)).isTrue();
    }

    @Test
    @DisplayName("休日判定: 日曜日はtrue")
    void isHoliday_sunday_returnsTrue() {
        var sunday = LocalDate.of(2026, 7, 19);
        assertThat(calendarService.isHoliday(sunday)).isTrue();
    }

    @Test
    @DisplayName("休日判定: 平日で会社カレンダーに登録なしはfalse")
    void isHoliday_weekdayNotRegistered_returnsFalse() {
        var monday = LocalDate.of(2026, 7, 13);
        when(calendarRepository.existsByHolidayDate(monday)).thenReturn(false);

        assertThat(calendarService.isHoliday(monday)).isFalse();
    }

    @Test
    @DisplayName("休日判定: 平日で会社カレンダーに登録ありはtrue")
    void isHoliday_weekdayRegistered_returnsTrue() {
        var monday = LocalDate.of(2026, 7, 13);
        when(calendarRepository.existsByHolidayDate(monday)).thenReturn(true);

        assertThat(calendarService.isHoliday(monday)).isTrue();
    }

    @Test
    @DisplayName("休日登録: 新規日付で正常に登録される")
    void createHoliday_newDate_createsSuccessfully() {
        var date = LocalDate.of(2026, 8, 13);
        when(calendarRepository.existsByHolidayDate(date)).thenReturn(false);
        when(calendarRepository.save(any(CompanyCalendar.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = calendarService.createHoliday(date, "お盆休み");

        assertThat(result.holidayDate()).isEqualTo(date);
        assertThat(result.holidayName()).isEqualTo("お盆休み");
        assertThat(result.fiscalYear()).isEqualTo(2026);
    }

    @Test
    @DisplayName("休日登録: 1-3月の日付は前年度の会計年度")
    void createHoliday_januaryDate_previousFiscalYear() {
        var date = LocalDate.of(2027, 1, 1);
        when(calendarRepository.existsByHolidayDate(date)).thenReturn(false);
        when(calendarRepository.save(any(CompanyCalendar.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = calendarService.createHoliday(date, "元日");

        assertThat(result.fiscalYear()).isEqualTo(2026);
    }

    @Test
    @DisplayName("休日登録: 既に登録済みの日付は409エラー")
    void createHoliday_duplicateDate_throwsConflict() {
        var date = LocalDate.of(2026, 8, 13);
        when(calendarRepository.existsByHolidayDate(date)).thenReturn(true);

        assertThatThrownBy(() -> calendarService.createHoliday(date, "お盆休み"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("既に休日として登録");
    }

    @Test
    @DisplayName("休日削除: 存在しないIDは404エラー")
    void deleteHoliday_notFound_throwsNotFound() {
        var id = UUID.randomUUID();
        when(calendarRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> calendarService.deleteHoliday(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("見つかりません");
    }

    @Test
    @DisplayName("休日削除: 存在するIDで正常に削除")
    void deleteHoliday_existingId_deletesSuccessfully() {
        var id = UUID.randomUUID();
        when(calendarRepository.existsById(id)).thenReturn(true);

        calendarService.deleteHoliday(id);

        verify(calendarRepository).deleteById(id);
    }

    @Test
    @DisplayName("休日一覧: 指定年度の休日が取得できる")
    void getHolidays_validYear_returnsList() {
        var calendar = CompanyCalendar.builder()
                .id(UUID.randomUUID())
                .holidayDate(LocalDate.of(2026, 8, 13))
                .holidayName("お盆休み")
                .fiscalYear(2026)
                .build();
        when(calendarRepository.findByFiscalYearOrderByHolidayDateAsc(2026))
                .thenReturn(List.of(calendar));

        var result = calendarService.getHolidays(2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).holidayName()).isEqualTo("お盆休み");
    }
}
