package com.example.attendance.application.service;

import com.example.attendance.common.enums.LeaveType;
import com.example.attendance.export.dto.PaidLeaveBalanceResponse;

import java.util.UUID;

public interface PaidLeaveService {

    PaidLeaveBalanceResponse getBalance(UUID userId);

    void grantAnnualLeave(UUID userId);

    void consume(UUID userId, LeaveType leaveType);

    boolean canApply(UUID userId, LeaveType leaveType);
}
