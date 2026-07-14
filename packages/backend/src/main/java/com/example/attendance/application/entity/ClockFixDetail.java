package com.example.attendance.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "clock_fix_details")
@Getter
@NoArgsConstructor
public class ClockFixDetail {

    @Id
    @Column(name = "application_id")
    private UUID applicationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "corrected_clock_in")
    private ZonedDateTime correctedClockIn;

    @Column(name = "corrected_clock_out")
    private ZonedDateTime correctedClockOut;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    public ClockFixDetail(Application application, LocalDate targetDate,
                          ZonedDateTime correctedClockIn, ZonedDateTime correctedClockOut,
                          String reason) {
        this.application = application;
        this.applicationId = application.getId();
        this.targetDate = targetDate;
        this.correctedClockIn = correctedClockIn;
        this.correctedClockOut = correctedClockOut;
        this.reason = reason;
    }
}
