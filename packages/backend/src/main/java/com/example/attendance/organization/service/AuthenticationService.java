package com.example.attendance.organization.service;

import com.example.attendance.organization.dto.LoginResponse;

import java.util.UUID;

public interface AuthenticationService {

    LoginResponse login(String email, String password);

    void changePassword(UUID userId, String currentPassword, String newPassword);
}
