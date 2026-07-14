package com.example.attendance.clock.repository;

import com.example.attendance.clock.entity.CompanyCalendar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CompanyCalendarRepositoryTest {

    @Autowired
    private CompanyCalendarRepository calendarRepository;

    @Test
    @DisplayName("existsByHolidayDate: 登録済みの日付はtrue")
    void existsByHolidayDate_registered_returnsTrue() {
        var calendar = CompanyCalendar.builder()
                .holidayDate(LocalDate.of(2026, 8, 13))
                .holidayName("お盆休み")
                .fiscalYear(2026)
                .build();
        calendarRepository.save(calendar);

        assertThat(calendarRepository.existsByHolidayDate(LocalDate.of(2026, 8, 13))).isTrue();
    }

    @Test
    @DisplayName("existsByHolidayDate: 未登録の日付はfalse")
    void existsByHolidayDate_notRegistered_returnsFalse() {
        assertThat(calendarRepository.existsByHolidayDate(LocalDate.of(2026, 8, 13))).isFalse();
    }

    @Test
    @DisplayName("findByFiscalYearOrderByHolidayDateAsc: 指定年度の休日を日付順で取得")
    void findByFiscalYear_multipleEntries_returnsSorted() {
        var calendar1 = CompanyCalendar.builder()
                .holidayDate(LocalDate.of(2026, 12, 30))
                .holidayName("年末休み")
                .fiscalYear(2026)
                .build();
        var calendar2 = CompanyCalendar.builder()
                .holidayDate(LocalDate.of(2026, 8, 13))
                .holidayName("お盆休み")
                .fiscalYear(2026)
                .build();
        calendarRepository.save(calendar1);
        calendarRepository.save(calendar2);

        var results = calendarRepository.findByFiscalYearOrderByHolidayDateAsc(2026);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getHolidayDate()).isBefore(results.get(1).getHolidayDate());
    }
}
