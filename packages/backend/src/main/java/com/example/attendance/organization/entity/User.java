package com.example.attendance.organization.entity;

import com.example.attendance.common.entity.BaseEntity;
import com.example.attendance.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "primary_department_id", nullable = false)
    private UUID primaryDepartmentId;

    @Column(name = "require_password_change", nullable = false)
    @Builder.Default
    private Boolean requirePasswordChange = true;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "has_attendance_record", nullable = false)
    @Builder.Default
    private Boolean hasAttendanceRecord = false;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private ZonedDateTime lockedUntil;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(ZonedDateTime.now());
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void lock(int lockMinutes) {
        this.lockedUntil = ZonedDateTime.now().plusMinutes(lockMinutes);
    }
}
