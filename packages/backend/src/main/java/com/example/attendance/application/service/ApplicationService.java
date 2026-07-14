package com.example.attendance.application.service;

import com.example.attendance.application.dto.ApplicationResponse;
import com.example.attendance.application.dto.ClockFixRequest;
import com.example.attendance.application.dto.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ApplicationService {

    ApplicationResponse submitClockFix(UUID userId, ClockFixRequest request);

    ApplicationResponse submitLeaveRequest(UUID userId, LeaveRequest request);

    ApplicationResponse approve(UUID applicationId, UUID approverId);

    ApplicationResponse reject(UUID applicationId, UUID approverId, String comment);

    ApplicationResponse withdraw(UUID applicationId, UUID userId);

    Page<ApplicationResponse> getMyApplications(UUID userId, String status, Pageable pageable);

    Page<ApplicationResponse> getPendingApplications(UUID approverId, Pageable pageable);
}
