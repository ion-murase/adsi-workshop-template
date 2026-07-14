package com.example.attendance.clock.service;

import com.example.attendance.application.service.OvertimeAlertService;
import com.example.attendance.clock.dto.ClockStatusResponse;
import com.example.attendance.clock.dto.MonthlySummaryResponse;
import com.example.attendance.clock.dto.TimeRecordResponse;
import com.example.attendance.clock.entity.TimeEntry;
import com.example.attendance.clock.entity.TimeRecord;
import com.example.attendance.clock.repository.TimeEntryRepository;
import com.example.attendance.clock.repository.TimeRecordRepository;
import com.example.attendance.common.enums.EntryType;
import com.example.attendance.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClockServiceImpl implements ClockService {

    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    private final TimeRecordRepository timeRecordRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final CalendarService calendarService;
    private final OvertimeAlertService overtimeAlertService;

    public ClockServiceImpl(TimeRecordRepository timeRecordRepository,
                            TimeEntryRepository timeEntryRepository,
                            CalendarService calendarService,
                            OvertimeAlertService overtimeAlertService) {
        this.timeRecordRepository = timeRecordRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.calendarService = calendarService;
        this.overtimeAlertService = overtimeAlertService;
    }

    @Override
    public TimeRecordResponse clockIn(UUID userId) {
        var today = LocalDate.now(TOKYO);
        var existing = timeRecordRepository.findByUserIdAndWorkDate(userId, today);

        if (existing.isPresent()) {
            throw new BusinessException("本日は既に出勤打刻済みです", HttpStatus.CONFLICT);
        }

        var now = ZonedDateTime.now(TOKYO);
        var isHoliday = calendarService.isHoliday(today);

        var record = TimeRecord.builder()
                .userId(userId)
                .workDate(today)
                .clockIn(now)
                .isHoliday(isHoliday)
                .timezone(TOKYO.getId())
                .build();

        var saved = timeRecordRepository.save(record);
        return TimeRecordResponse.from(saved);
    }

    @Override
    public TimeRecordResponse clockOut(UUID userId) {
        var today = LocalDate.now(TOKYO);
        var record = timeRecordRepository.findByUserIdAndWorkDate(userId, today)
                .orElseThrow(() -> new BusinessException("出勤打刻がありません", HttpStatus.BAD_REQUEST));

        if (record.getClockOut() != null) {
            throw new BusinessException("本日は既に退勤打刻済みです", HttpStatus.CONFLICT);
        }

        var now = ZonedDateTime.now(TOKYO);
        record.setClockOut(now);

        var result = WorkTimeCalculator.calculate(
                record.getClockIn(), now, record.getBreakMinutes(), record.getIsHoliday());

        record.setWorkMinutes(result.workMinutes());
        record.setOvertimeMinutes(result.overtimeMinutes());
        record.setNightMinutes(result.nightMinutes());
        record.setHolidayWorkMinutes(result.holidayWorkMinutes());

        var saved = timeRecordRepository.save(record);

        overtimeAlertService.checkAndNotify(userId, YearMonth.from(today));

        return TimeRecordResponse.from(saved);
    }

    @Override
    public TimeRecordResponse goOut(UUID userId) {
        var record = getTodayRecord(userId);
        validateNotClockedOut(record);

        var entry = TimeEntry.builder()
                .timeRecord(record)
                .entryType(EntryType.GO_OUT)
                .recordedAt(ZonedDateTime.now(TOKYO))
                .build();

        timeEntryRepository.save(entry);
        return TimeRecordResponse.from(record);
    }

    @Override
    public TimeRecordResponse returnFromOut(UUID userId) {
        var record = getTodayRecord(userId);
        validateNotClockedOut(record);

        var entries = timeEntryRepository.findByTimeRecordIdOrderByRecordedAtAsc(record.getId());
        var lastEntry = entries.isEmpty() ? null : entries.get(entries.size() - 1);

        if (lastEntry == null || lastEntry.getEntryType() != EntryType.GO_OUT) {
            throw new BusinessException("外出打刻がありません", HttpStatus.BAD_REQUEST);
        }

        var entry = TimeEntry.builder()
                .timeRecord(record)
                .entryType(EntryType.RETURN)
                .recordedAt(ZonedDateTime.now(TOKYO))
                .build();

        timeEntryRepository.save(entry);
        return TimeRecordResponse.from(record);
    }

    @Override
    @Transactional(readOnly = true)
    public ClockStatusResponse getStatus(UUID userId) {
        var today = LocalDate.now(TOKYO);
        var recordOpt = timeRecordRepository.findByUserIdAndWorkDate(userId, today);

        if (recordOpt.isEmpty()) {
            return new ClockStatusResponse(today, null, null, "NOT_CLOCKED_IN", List.of());
        }

        var record = recordOpt.get();
        var entries = timeEntryRepository.findByTimeRecordIdOrderByRecordedAtAsc(record.getId());

        var state = determineState(record, entries);
        var entryRecords = entries.stream()
                .map(e -> new ClockStatusResponse.EntryRecord(
                        e.getEntryType().name(), e.getRecordedAt()))
                .toList();

        return new ClockStatusResponse(
                record.getWorkDate(), record.getClockIn(), record.getClockOut(),
                state, entryRecords);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeRecordResponse> getMonthlyRecords(UUID userId, int year, int month) {
        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return timeRecordRepository.findByUserIdAndYearMonth(userId, startDate, endDate)
                .stream()
                .map(TimeRecordResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(UUID userId, int year, int month) {
        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        var records = timeRecordRepository.findByUserIdAndYearMonth(userId, startDate, endDate);

        int workDays = records.size();
        int totalWork = records.stream().mapToInt(r -> r.getWorkMinutes() != null ? r.getWorkMinutes() : 0).sum();
        int totalOvertime = records.stream().mapToInt(r -> r.getOvertimeMinutes() != null ? r.getOvertimeMinutes() : 0).sum();
        int totalNight = records.stream().mapToInt(r -> r.getNightMinutes() != null ? r.getNightMinutes() : 0).sum();
        int totalHoliday = records.stream().mapToInt(r -> r.getHolidayWorkMinutes() != null ? r.getHolidayWorkMinutes() : 0).sum();

        return new MonthlySummaryResponse(year, month, workDays, totalWork, totalOvertime, totalNight, totalHoliday);
    }

    private TimeRecord getTodayRecord(UUID userId) {
        var today = LocalDate.now(TOKYO);
        return timeRecordRepository.findByUserIdAndWorkDate(userId, today)
                .orElseThrow(() -> new BusinessException("出勤打刻がありません", HttpStatus.BAD_REQUEST));
    }

    private void validateNotClockedOut(TimeRecord record) {
        if (record.getClockOut() != null) {
            throw new BusinessException("既に退勤済みです", HttpStatus.CONFLICT);
        }
    }

    private String determineState(TimeRecord record, List<TimeEntry> entries) {
        if (record.getClockOut() != null) {
            return "CLOCKED_OUT";
        }
        if (!entries.isEmpty()) {
            var last = entries.get(entries.size() - 1);
            if (last.getEntryType() == EntryType.GO_OUT) {
                return "OUT";
            }
        }
        return "WORKING";
    }
}
