package com.example.attendance.clock.service;

import com.example.attendance.clock.dto.HolidayResponse;
import com.example.attendance.clock.entity.CompanyCalendar;
import com.example.attendance.clock.repository.CompanyCalendarRepository;
import com.example.attendance.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CalendarServiceImpl implements CalendarService {

    private final CompanyCalendarRepository calendarRepository;

    public CalendarServiceImpl(CompanyCalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidays(int year) {
        return calendarRepository.findByFiscalYearOrderByHolidayDateAsc(year)
                .stream()
                .map(HolidayResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isHoliday(LocalDate date) {
        var dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return true;
        }
        return calendarRepository.existsByHolidayDate(date);
    }

    @Override
    public HolidayResponse createHoliday(LocalDate date, String name) {
        if (calendarRepository.existsByHolidayDate(date)) {
            throw new BusinessException("指定された日付は既に休日として登録されています", HttpStatus.CONFLICT);
        }

        int fiscalYear = determineFiscalYear(date);

        var calendar = CompanyCalendar.builder()
                .holidayDate(date)
                .holidayName(name)
                .fiscalYear(fiscalYear)
                .build();

        var saved = calendarRepository.save(calendar);
        return HolidayResponse.from(saved);
    }

    @Override
    public void deleteHoliday(UUID id) {
        if (!calendarRepository.existsById(id)) {
            throw new BusinessException("指定された休日が見つかりません", HttpStatus.NOT_FOUND);
        }
        calendarRepository.deleteById(id);
    }

    private int determineFiscalYear(LocalDate date) {
        return date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1;
    }
}
