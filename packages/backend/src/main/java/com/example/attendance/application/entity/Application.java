package com.example.attendance.application.entity;

import com.example.attendance.common.entity.BaseEntity;
import com.example.attendance.common.enums.ApplicationStatus;
import com.example.attendance.common.enums.ApplicationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
@NoArgsConstructor
public class Application extends BaseEntity {

    @Column(name = "applicant_id", nullable = false)
    private UUID applicantId;

    @Column(name = "approver_id")
    private UUID approverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "rejection_comment", columnDefinition = "TEXT")
    private String rejectionComment;

    @Column(name = "applied_at", nullable = false)
    private ZonedDateTime appliedAt;

    @Column(name = "decided_at")
    private ZonedDateTime decidedAt;

    @Version
    private Long version;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ClockFixDetail clockFixDetail;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LeaveRequestDetail leaveRequestDetail;

    public Application(UUID applicantId, ApplicationType type) {
        this.applicantId = applicantId;
        this.type = type;
        this.status = ApplicationStatus.PENDING;
        this.appliedAt = ZonedDateTime.now();
    }

    public void approve(UUID approverId) {
        this.approverId = approverId;
        this.status = ApplicationStatus.APPROVED;
        this.decidedAt = ZonedDateTime.now();
    }

    public void reject(UUID approverId, String comment) {
        this.approverId = approverId;
        this.status = ApplicationStatus.REJECTED;
        this.rejectionComment = comment;
        this.decidedAt = ZonedDateTime.now();
    }

    public void withdraw() {
        this.status = ApplicationStatus.WITHDRAWN;
        this.decidedAt = ZonedDateTime.now();
    }

    public void setClockFixDetail(ClockFixDetail detail) {
        this.clockFixDetail = detail;
    }

    public void setLeaveRequestDetail(LeaveRequestDetail detail) {
        this.leaveRequestDetail = detail;
    }
}
