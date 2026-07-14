package com.example.attendance.application.entity;

import com.example.attendance.common.enums.LeaveType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_request_details")
@Getter
@NoArgsConstructor
public class LeaveRequestDetail {

    @Id
    @Column(name = "application_id")
    private UUID applicationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Column(name = "leave_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    public LeaveRequestDetail(Application application, LocalDate leaveDate, LeaveType leaveType) {
        this.application = application;
        this.applicationId = application.getId();
        this.leaveDate = leaveDate;
        this.leaveType = leaveType;
    }
}
