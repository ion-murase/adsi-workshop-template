package com.example.attendance.organization.service;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import com.example.attendance.organization.dto.LoginResponse;
import com.example.attendance.organization.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public LoginResponse login(String email, String password) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        "メールアドレスまたはパスワードが正しくありません", HttpStatus.UNAUTHORIZED));

        if (!user.getActive()) {
            throw new BusinessException(
                    "メールアドレスまたはパスワードが正しくありません", HttpStatus.UNAUTHORIZED);
        }

        if (user.isLocked()) {
            throw new BusinessException("アカウントがロックされています", HttpStatus.LOCKED);
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.incrementFailedAttempts();
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lock(LOCK_MINUTES);
            }
            userRepository.save(user);
            throw new BusinessException(
                    "メールアドレスまたはパスワードが正しくありません", HttpStatus.UNAUTHORIZED);
        }

        user.resetFailedAttempts();
        userRepository.save(user);

        var token = jwtTokenProvider.generateToken(
                user.getId(), user.getEmail(), user.getRole().name());

        return new LoginResponse(
                token,
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getRequirePasswordChange()
        );
    }

    @Override
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("ユーザが見つかりません", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("現在のパスワードが正しくありません", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setRequirePasswordChange(false);
        userRepository.save(user);
    }
}
