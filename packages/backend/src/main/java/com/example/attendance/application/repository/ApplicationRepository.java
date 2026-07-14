package com.example.attendance.application.repository;

import com.example.attendance.application.entity.Application;
import com.example.attendance.common.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Page<Application> findByApplicantIdOrderByAppliedAtDesc(UUID applicantId, Pageable pageable);

    Page<Application> findByApplicantIdAndStatusOrderByAppliedAtDesc(
            UUID applicantId, ApplicationStatus status, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.status = 'PENDING' " +
           "AND a.applicantId IN (SELECT u.id FROM User u WHERE u.primaryDepartmentId = :departmentId) " +
           "ORDER BY a.appliedAt ASC")
    Page<Application> findPendingByDepartment(@Param("departmentId") UUID departmentId, Pageable pageable);

    @Query("SELECT u.id, u.name FROM User u WHERE u.id IN :userIds")
    List<Object[]> findUserNamesByIds(@Param("userIds") List<UUID> userIds);
}
