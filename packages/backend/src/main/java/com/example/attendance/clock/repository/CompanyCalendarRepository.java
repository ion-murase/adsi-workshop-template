package com.example.attendance.clock.repository;

import com.example.attendance.clock.entity.CompanyCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CompanyCalendarRepository extends JpaRepository<CompanyCalendar, UUID> {

    List<CompanyCalendar> findByFiscalYearOrderByHolidayDateAsc(Integer fiscalYear);

    boolean existsByHolidayDate(LocalDate holidayDate);
}
