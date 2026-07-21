package com.example.attendance.organization.service;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.organization.dto.UserRequest;
import com.example.attendance.organization.dto.UserResponse;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementServiceImpl(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        return userRepository.findByActiveOrderByNameAsc(true)
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        var user = findUser(id);
        return UserResponse.from(user);
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("このメールアドレスは既に登録されています", HttpStatus.CONFLICT);
        }

        var tempPassword = generateTempPassword();
        var user = User.builder()
                .email(request.email())
                .name(request.name())
                .role(request.role())
                .primaryDepartmentId(request.primaryDepartmentId())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .requirePasswordChange(true)
                .build();

        var saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Override
    public UserResponse updateUser(UUID id, UserRequest request) {
        var user = findUser(id);

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new BusinessException("このメールアドレスは既に登録されています", HttpStatus.CONFLICT);
        }

        user.setEmail(request.email());
        user.setName(request.name());
        user.setRole(request.role());
        user.setPrimaryDepartmentId(request.primaryDepartmentId());

        var saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Override
    public void deleteUser(UUID id) {
        var user = findUser(id);

        if (user.getHasAttendanceRecord()) {
            user.setActive(false);
            userRepository.save(user);
        } else {
            userRepository.delete(user);
        }
    }

    @Override
    public String resetPassword(UUID id) {
        var user = findUser(id);
        var tempPassword = generateTempPassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setRequirePasswordChange(true);
        userRepository.save(user);
        return tempPassword;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        return getUserById(userId);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ユーザが見つかりません", HttpStatus.NOT_FOUND));
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }
}
