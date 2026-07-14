package com.example.attendance.clock.repository;

import com.example.attendance.clock.entity.TimeRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/test-data-clock.sql")
class TimeRecordRepositoryTest {

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Test
    @DisplayName("findByUserIdAndWorkDate: レコードが存在する場合に取得できる")
    void findByUserIdAndWorkDate_existing_returnsRecord() {
        var today = LocalDate.of(2026, 7, 14);
        var record = TimeRecord.builder()
                .userId(USER_ID)
                .workDate(today)
                .clockIn(ZonedDateTime.of(2026, 7, 14, 9, 0, 0, 0, TOKYO))
                .timezone(TOKYO.getId())
                .build();
        timeRecordRepository.save(record);

        var result = timeRecordRepository.findByUserIdAndWorkDate(USER_ID, today);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("findByUserIdAndWorkDate: レコードが存在しない場合はempty")
    void findByUserIdAndWorkDate_notExisting_returnsEmpty() {
        var result = timeRecordRepository.findByUserIdAndWorkDate(USER_ID, LocalDate.of(2026, 7, 14));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndYearMonth: 月内のレコードが日付順で取得できる")
    void findByUserIdAndYearMonth_multipleRecords_returnsSorted() {
        var record1 = TimeRecord.builder()
                .userId(USER_ID)
                .workDate(LocalDate.of(2026, 7, 1))
                .clockIn(ZonedDateTime.of(2026, 7, 1, 9, 0, 0, 0, TOKYO))
                .timezone(TOKYO.getId())
                .build();
        var record2 = TimeRecord.builder()
                .userId(USER_ID)
                .workDate(LocalDate.of(2026, 7, 3))
                .clockIn(ZonedDateTime.of(2026, 7, 3, 9, 0, 0, 0, TOKYO))
                .timezone(TOKYO.getId())
                .build();
        timeRecordRepository.save(record1);
        timeRecordRepository.save(record2);

        var results = timeRecordRepository.findByUserIdAndYearMonth(
                USER_ID, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getWorkDate()).isBefore(results.get(1).getWorkDate());
    }

    @Test
    @DisplayName("findByUserIdAndYearMonth: 他ユーザのレコードは含まない")
    void findByUserIdAndYearMonth_otherUser_excluded() {
        var otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        var record = TimeRecord.builder()
                .userId(otherUserId)
                .workDate(LocalDate.of(2026, 7, 1))
                .clockIn(ZonedDateTime.of(2026, 7, 1, 9, 0, 0, 0, TOKYO))
                .timezone(TOKYO.getId())
                .build();
        timeRecordRepository.save(record);

        var results = timeRecordRepository.findByUserIdAndYearMonth(
                USER_ID, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        assertThat(results).isEmpty();
    }
}
