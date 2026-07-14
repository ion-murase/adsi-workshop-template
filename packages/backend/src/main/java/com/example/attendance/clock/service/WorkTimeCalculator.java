package com.example.attendance.clock.service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public final class WorkTimeCalculator {

    private static final int STANDARD_WORK_MINUTES = 450;
    private static final int HALF_DAY_WORK_MINUTES = 225;
    private static final int NIGHT_START_HOUR = 22;
    private static final int NIGHT_END_HOUR = 5;

    private WorkTimeCalculator() {}

    public static Result calculate(ZonedDateTime clockIn, ZonedDateTime clockOut,
                                   int breakMinutes, boolean isHoliday) {
        var truncatedIn = clockIn.truncatedTo(ChronoUnit.MINUTES);
        var truncatedOut = clockOut.truncatedTo(ChronoUnit.MINUTES);

        int totalMinutes = (int) ChronoUnit.MINUTES.between(truncatedIn, truncatedOut);
        int workMinutes = Math.max(0, totalMinutes - breakMinutes);
        int overtimeMinutes = Math.max(0, workMinutes - STANDARD_WORK_MINUTES);
        int nightMinutes = calculateNightMinutes(truncatedIn, truncatedOut);
        int holidayWorkMinutes = isHoliday ? workMinutes : 0;

        return new Result(workMinutes, overtimeMinutes, nightMinutes, holidayWorkMinutes);
    }

    static int calculateNightMinutes(ZonedDateTime start, ZonedDateTime end) {
        int nightMinutes = 0;
        var current = start;

        while (current.isBefore(end)) {
            int hour = current.getHour();
            if (hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR) {
                nightMinutes++;
            }
            current = current.plusMinutes(1);
        }

        return nightMinutes;
    }

    public record Result(
            int workMinutes,
            int overtimeMinutes,
            int nightMinutes,
            int holidayWorkMinutes
    ) {}
}
