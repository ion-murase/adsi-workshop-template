package com.example.attendance.organization.repository;

import com.example.attendance.organization.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByActiveOrderByNameAsc(Boolean active);

    List<User> findByPrimaryDepartmentIdAndActiveOrderByNameAsc(UUID departmentId, Boolean active);
}
