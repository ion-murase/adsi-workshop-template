package com.example.attendance.organization.repository;

import com.example.attendance.organization.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findBySiteIdOrderByNameAsc(UUID siteId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM User u WHERE u.primaryDepartmentId = :departmentId AND u.active = true")
    boolean hasActiveUsers(@Param("departmentId") UUID departmentId);
}
