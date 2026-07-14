package com.example.attendance.organization.service;

import com.example.attendance.organization.dto.UserRequest;
import com.example.attendance.organization.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserManagementService {

    List<UserResponse> getActiveUsers();

    UserResponse getUserById(UUID id);

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(UUID id, UserRequest request);

    void deleteUser(UUID id);

    String resetPassword(UUID id);

    UserResponse getMe(UUID userId);
}
