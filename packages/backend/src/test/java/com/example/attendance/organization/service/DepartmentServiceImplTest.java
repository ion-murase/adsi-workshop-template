package com.example.attendance.organization.service;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.organization.dto.DepartmentRequest;
import com.example.attendance.organization.entity.Department;
import com.example.attendance.organization.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    private DepartmentServiceImpl departmentService;

    private static final UUID SITE_ID = UUID.randomUUID();
    private static final UUID DEPT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentServiceImpl(departmentRepository);
    }

    @Test
    @DisplayName("全部署取得: 全件がレスポンスに変換される")
    void getAllDepartments_returnsMappedResponses() {
        var dept = Department.builder().name("開発部").siteId(SITE_ID).build();
        when(departmentRepository.findAll()).thenReturn(List.of(dept));

        var result = departmentService.getAllDepartments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("開発部");
    }

    @Test
    @DisplayName("部署作成: リクエストから部署が保存される")
    void createDepartment_savesAndReturnsResponse() {
        var request = new DepartmentRequest("営業部", SITE_ID);
        var saved = Department.builder().name("営業部").siteId(SITE_ID).build();
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        var result = departmentService.createDepartment(request);

        assertThat(result.name()).isEqualTo("営業部");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("部署更新: 存在するIDで更新するとレスポンスが返される")
    void updateDepartment_existingId_updatesAndReturns() {
        var dept = Department.builder().name("旧名").siteId(SITE_ID).build();
        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(dept));
        when(departmentRepository.save(any(Department.class))).thenReturn(dept);

        var request = new DepartmentRequest("新名", SITE_ID);
        var result = departmentService.updateDepartment(DEPT_ID, request);

        assertThat(result.name()).isEqualTo("新名");
    }

    @Test
    @DisplayName("部署更新: 存在しないIDで例外が投げられる")
    void updateDepartment_nonExistingId_throwsException() {
        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.empty());

        var request = new DepartmentRequest("新名", SITE_ID);

        assertThatThrownBy(() -> departmentService.updateDepartment(DEPT_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("部署が見つかりません");
    }

    @Test
    @DisplayName("部署削除: 所属ユーザなしの部署は削除できる")
    void deleteDepartment_noActiveUsers_deletesSuccessfully() {
        var dept = Department.builder().name("削除部").siteId(SITE_ID).build();
        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(dept));
        when(departmentRepository.hasActiveUsers(DEPT_ID)).thenReturn(false);

        departmentService.deleteDepartment(DEPT_ID);

        verify(departmentRepository).delete(dept);
    }

    @Test
    @DisplayName("部署削除: 所属ユーザありの部署は削除不可")
    void deleteDepartment_hasActiveUsers_throwsException() {
        var dept = Department.builder().name("削除不可部").siteId(SITE_ID).build();
        when(departmentRepository.findById(DEPT_ID)).thenReturn(Optional.of(dept));
        when(departmentRepository.hasActiveUsers(DEPT_ID)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.deleteDepartment(DEPT_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("所属ユーザが存在するため削除できません");
    }
}
