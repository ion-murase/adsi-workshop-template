package com.example.attendance.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "user_departments")
@IdClass(UserDepartment.UserDepartmentId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDepartment {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "department_id")
    private UUID departmentId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDepartmentId implements Serializable {
        private UUID userId;
        private UUID departmentId;
    }
}
