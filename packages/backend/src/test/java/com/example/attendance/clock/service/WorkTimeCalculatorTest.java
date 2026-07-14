package com.example.attendance.clock.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class WorkTimeCalculatorTest {

    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    @Test
    @DisplayName("通常勤務: 9:00-18:00 → 実労働480min, 残業30min, 深夜0min")
    void calculate_normalWork_returnsCorrectMinutes() {
        var clockIn = ZonedDateTime.of(2026, 7, 14, 9, 0, 0, 0, TOKYO);
        var clockOut = ZonedDateTime.of(2026, 7, 14, 18, 0, 0, 0, TOKYO);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut, 60, false);

        assertThat(result.workMinutes()).isEqualTo(480);
        assertThat(result.overtimeMinutes()).isEqualTo(30);
        assertThat(result.nightMinutes()).isEqualTo(0);
        assertThat(result.holidayWorkMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("定時勤務: 9:00-17:30 → 実労働450min, 残業0min")
    void calculate_exactStandard_noOvertime() {
        var clockIn = ZonedDateTime.of(2026, 7, 14, 9, 0, 0, 0, TOKYO);
        var clockOut = ZonedDateTime.of(2026, 7, 14, 17, 30, 0, 0, TOKYO);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut, 60, false);

        assertThat(result.workMinutes()).isEqualTo(450);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("深夜勤務: 20:00-翌2:00 → 深夜240min(22:00-2:00)")
    void calculate_nightWork_returnsNightMinutes() {
        var clockIn = ZonedDateTime.of(2026, 7, 14, 20, 0, 0, 0, TOKYO);
        var clockOut = ZonedDateTime.of(2026, 7, 15, 2, 0, 0, 0, TOKYO);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut, 60, false);

        assertThat(result.nightMinutes()).isEqualTo(240);
    }

    @Test
    @DisplayName("休日勤務: 実労働時間が全て休日勤務扱い")
    void calculate_holidayWork_allMinutesAreHoliday() {
        var clockIn = ZonedDateTime.of(2026, 7, 14, 9, 0, 0, 0, TOKYO);
        var clockOut = ZonedDateTime.of(2026, 7, 14, 18, 0, 0, 0, TOKYO);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut, 60, true);

        assertThat(result.holidayWorkMinutes()).isEqualTo(480);
    }

    @Test
    @DisplayName("短時間勤務: 3時間未満で休憩控除すると0分")
    void calculate_shortWork_minimumZero() {
        var clockIn = ZonedDateTime.of(2026, 7, 14, 9, 0, 0, 0, TOKYO);
        var clockOut = ZonedDateTime.of(2026, 7, 14, 9, 30, 0, 0, TOKYO);

        var result = WorkTimeCalculator.calculate(clockIn, clockOut, 60, false);

        assertThat(result.workMinutes()).isEqualTo(0);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("nightMinutesProvider")
    @DisplayName("深夜時間帯の計算パターン")
    void calculate_nightMinutes_variousPatterns(
            ZonedDateTime clockIn, ZonedDateTime clockOut, int expectedNightMinutes) {
        var result = WorkTimeCalculator.calculate(clockIn, clockOut, 60, false);
        assertThat(result.nightMinutes()).isEqualTo(expectedNightMinutes);
    }

    static Stream<Arguments> nightMinutesProvider() {
        return Stream.of(
                // 日中のみ（深夜なし）
                Arguments.of(
                        ZonedDateTime.of(2026, 7, 14, 9, 0, 0, 0, TOKYO),
                        ZonedDateTime.of(2026, 7, 14, 17, 0, 0, 0, TOKYO),
                        0),
                // 22:00-23:00（深夜60分）
                Arguments.of(
                        ZonedDateTime.of(2026, 7, 14, 21, 0, 0, 0, TOKYO),
                        ZonedDateTime.of(2026, 7, 14, 23, 0, 0, 0, TOKYO),
                        60),
                // 深夜帯のみ: 0:00-5:00（深夜300分）
                Arguments.of(
                        ZonedDateTime.of(2026, 7, 14, 0, 0, 0, 0, TOKYO),
                        ZonedDateTime.of(2026, 7, 14, 5, 0, 0, 0, TOKYO),
                        300)
        );
    }
}
