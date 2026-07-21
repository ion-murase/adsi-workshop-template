package com.example.attendance.organization.service;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.enums.Role;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthenticationServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthenticationServiceImpl(userRepository, passwordEncoder, jwtTokenProvider);
    }

    private User createTestUser() {
        return User.builder()
                .email("test@example.com")
                .passwordHash("hashed")
                .name("テストユーザ")
                .role(Role.GENERAL)
                .primaryDepartmentId(UUID.randomUUID())
                .active(true)
                .failedLoginAttempts(0)
                .requirePasswordChange(false)
                .build();
    }

    @Test
    @DisplayName("ログイン成功: 正しい認証情報でトークンが返される")
    void login_validCredentials_returnsToken() {
        var user = createTestUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(), anyString(), anyString())).thenReturn("jwt-token");
        when(userRepository.save(any())).thenReturn(user);

        var result = authService.login("test@example.com", "password");

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("ログイン失敗: メールが存在しない場合401")
    void login_emailNotFound_throwsUnauthorized() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("unknown@example.com", "password"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("メールアドレスまたはパスワードが正しくありません");
    }

    @Test
    @DisplayName("ログイン失敗: パスワード不一致で失敗回数がインクリメント")
    void login_wrongPassword_incrementsFailedAttempts() {
        var user = createTestUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        assertThatThrownBy(() -> authService.login("test@example.com", "wrong"))
                .isInstanceOf(BusinessException.class);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("ログイン失敗: 5回失敗でアカウントロック")
    void login_fifthFailure_locksAccount() {
        var user = createTestUser();
        user.setFailedLoginAttempts(4);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        assertThatThrownBy(() -> authService.login("test@example.com", "wrong"))
                .isInstanceOf(BusinessException.class);

        assertThat(user.getLockedUntil()).isNotNull();
        assertThat(user.getLockedUntil()).isAfter(ZonedDateTime.now());
    }

    @Test
    @DisplayName("ログイン失敗: ロック中はログイン不可")
    void login_lockedAccount_throwsLocked() {
        var user = createTestUser();
        user.setLockedUntil(ZonedDateTime.now().plusMinutes(30));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login("test@example.com", "password"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ロックされています");
    }

    @Test
    @DisplayName("ログイン失敗: 無効化ユーザはログイン不可")
    void login_inactiveUser_throwsUnauthorized() {
        var user = createTestUser();
        user.setActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login("test@example.com", "password"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("メールアドレスまたはパスワードが正しくありません");
    }

    @Test
    @DisplayName("パスワード変更: 正しい現パスワードで変更成功")
    void changePassword_validCurrent_success() {
        var user = createTestUser();
        user.setRequirePasswordChange(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("new-hashed");
        when(userRepository.save(any())).thenReturn(user);

        authService.changePassword(user.getId(), "current", "newpass123");

        assertThat(user.getPasswordHash()).isEqualTo("new-hashed");
        assertThat(user.getRequirePasswordChange()).isFalse();
    }

    @Test
    @DisplayName("パスワード変更: 現パスワードが不正で失敗")
    void changePassword_wrongCurrent_throwsBadRequest() {
        var user = createTestUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(user.getId(), "wrong", "newpass123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("現在のパスワードが正しくありません");
    }
}
