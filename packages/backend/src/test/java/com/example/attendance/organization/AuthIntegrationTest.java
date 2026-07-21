package com.example.attendance.organization;

import com.example.attendance.common.enums.Role;
import com.example.attendance.organization.dto.LoginRequest;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql("/test-data-auth.sql")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final UUID DEPT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            var admin = User.builder()
                    .email("admin@example.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .name("管理者")
                    .role(Role.ADMIN)
                    .primaryDepartmentId(DEPT_ID)
                    .requirePasswordChange(false)
                    .active(true)
                    .hasAttendanceRecord(false)
                    .failedLoginAttempts(0)
                    .build();
            userRepository.save(admin);
        }
    }

    @Test
    @DisplayName("ログイン成功 → トークンでユーザ情報取得")
    void loginAndAccessProtectedResource() throws Exception {
        var loginRequest = new LoginRequest("admin@example.com", "password123");

        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();

        var token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    @Test
    @DisplayName("ログイン失敗: 不正なパスワード")
    void login_wrongPassword_returns401() throws Exception {
        var loginRequest = new LoginRequest("admin@example.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("認証なしで保護リソースにアクセス: 403")
    void accessProtected_noToken_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
}
