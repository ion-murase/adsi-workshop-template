package com.example.attendance.export.dto;

import java.math.BigDecimal;

public record PaidLeaveBalanceResponse(
        int fiscalYear,
        BigDecimal grantedDays,
        BigDecimal usedDays,
        BigDecimal carriedOverDays,
        BigDecimal remainingDays
) {}
