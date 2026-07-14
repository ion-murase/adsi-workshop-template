package com.example.attendance.application.repository;

import com.example.attendance.application.entity.PaidLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaidLeaveBalanceRepository extends JpaRepository<PaidLeaveBalance, UUID> {

    Optional<PaidLeaveBalance> findByUserIdAndFiscalYear(UUID userId, Integer fiscalYear);

    Optional<PaidLeaveBalance> findTopByUserIdOrderByFiscalYearDesc(UUID userId);
}
