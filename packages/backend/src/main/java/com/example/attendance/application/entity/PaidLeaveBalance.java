package com.example.attendance.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "paid_leave_balances")
@Getter
@NoArgsConstructor
public class PaidLeaveBalance {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "granted_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal grantedDays;

    @Column(name = "used_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal usedDays;

    @Column(name = "carried_over_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal carriedOverDays;

    @Column(name = "expires_at", nullable = false)
    private LocalDate expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public PaidLeaveBalance(UUID userId, int fiscalYear, BigDecimal grantedDays,
                            BigDecimal carriedOverDays, LocalDate expiresAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.fiscalYear = fiscalYear;
        this.grantedDays = grantedDays;
        this.usedDays = BigDecimal.ZERO;
        this.carriedOverDays = carriedOverDays;
        this.expiresAt = expiresAt;
    }

    public BigDecimal getRemainingDays() {
        return grantedDays.add(carriedOverDays).subtract(usedDays);
    }

    public void consume(BigDecimal days) {
        this.usedDays = this.usedDays.add(days);
    }

    @PrePersist
    protected void onCreate() {
        var now = ZonedDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }
}
