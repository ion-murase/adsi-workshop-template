package com.example.attendance.clock.repository;

import com.example.attendance.clock.entity.TimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, UUID> {

    Optional<TimeRecord> findByUserIdAndWorkDate(UUID userId, LocalDate workDate);

    @Query("SELECT tr FROM TimeRecord tr WHERE tr.userId = :userId " +
           "AND tr.workDate >= :startDate AND tr.workDate <= :endDate " +
           "ORDER BY tr.workDate ASC")
    List<TimeRecord> findByUserIdAndYearMonth(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
