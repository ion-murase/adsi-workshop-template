package com.example.attendance.organization.service;

import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.organization.dto.UserRequest;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserManagementServiceImpl userService;

    private static final UUID DEPT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userService = new UserManagementServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("ユーザ登録: 新規メールで正常に作成")
    void createUser_newEmail_success() {
        var request = new UserRequest("new@example.com", "新規ユーザ", Role.GENERAL, DEPT_ID);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = userService.createUser(request);

        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.requirePasswordChange()).isTrue();
    }

    @Test
    @DisplayName("ユーザ登録: 重複メールで409")
    void createUser_duplicateEmail_throwsConflict() {
        var request = new UserRequest("dup@example.com", "重複ユーザ", Role.GENERAL, DEPT_ID);
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("既に登録されています");
    }

    @Test
    @DisplayName("ユーザ削除: 勤怠記録ありの場合は無効化")
    void deleteUser_hasAttendance_deactivates() {
        var user = User.builder()
                .email("user@example.com")
                .name("ユーザ")
                .role(Role.GENERAL)
                .primaryDepartmentId(DEPT_ID)
                .passwordHash("hash")
                .active(true)
                .hasAttendanceRecord(true)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.deleteUser(user.getId());

        assertThat(user.getActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("ユーザ削除: 勤怠記録なしの場合は物理削除")
    void deleteUser_noAttendance_physicallyDeletes() {
        var user = User.builder()
                .email("user@example.com")
                .name("ユーザ")
                .role(Role.GENERAL)
                .primaryDepartmentId(DEPT_ID)
                .passwordHash("hash")
                .active(true)
                .hasAttendanceRecord(false)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteUser(user.getId());

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("パスワードリセット: 仮パスワードが発行される")
    void resetPassword_success_returnsTempPassword() {
        var user = User.builder()
                .email("user@example.com")
                .name("ユーザ")
                .role(Role.GENERAL)
                .primaryDepartmentId(DEPT_ID)
                .passwordHash("old-hash")
                .active(true)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("new-hash");
        when(userRepository.save(any())).thenReturn(user);

        var tempPassword = userService.resetPassword(user.getId());

        assertThat(tempPassword).hasSize(12);
        assertThat(user.getRequirePasswordChange()).isTrue();
    }
}
