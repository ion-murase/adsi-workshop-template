package com.example.attendance.application.repository;

import com.example.attendance.application.entity.LeaveRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeaveRequestDetailRepository extends JpaRepository<LeaveRequestDetail, UUID> {
}
