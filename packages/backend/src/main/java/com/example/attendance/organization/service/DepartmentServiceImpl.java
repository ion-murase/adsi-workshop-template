package com.example.attendance.organization.service;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.organization.dto.DepartmentRequest;
import com.example.attendance.organization.dto.DepartmentResponse;
import com.example.attendance.organization.entity.Department;
import com.example.attendance.organization.repository.DepartmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        var department = Department.builder()
                .name(request.name())
                .siteId(request.siteId())
                .build();

        var saved = departmentRepository.save(department);
        return DepartmentResponse.from(saved);
    }

    @Override
    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest request) {
        var department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部署が見つかりません", HttpStatus.NOT_FOUND));

        department.setName(request.name());
        department.setSiteId(request.siteId());

        var saved = departmentRepository.save(department);
        return DepartmentResponse.from(saved);
    }

    @Override
    public void deleteDepartment(UUID id) {
        var department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部署が見つかりません", HttpStatus.NOT_FOUND));

        if (departmentRepository.hasActiveUsers(id)) {
            throw new BusinessException("所属ユーザが存在するため削除できません", HttpStatus.CONFLICT);
        }

        departmentRepository.delete(department);
    }
}
