package com.example.attendance.clock.repository;

import com.example.attendance.clock.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByTimeRecordIdOrderByRecordedAtAsc(UUID timeRecordId);
}
