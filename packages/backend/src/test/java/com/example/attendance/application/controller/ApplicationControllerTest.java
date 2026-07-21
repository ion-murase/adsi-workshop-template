package com.example.attendance.application.controller;

import com.example.attendance.application.dto.ApplicationResponse;
import com.example.attendance.application.dto.ClockFixRequest;
import com.example.attendance.application.service.ApplicationService;
import com.example.attendance.common.enums.ApplicationStatus;
import com.example.attendance.common.enums.ApplicationType;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.infrastructure.security.JwtAuthFilter;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ApplicationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}),
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@ActiveProfiles("test")
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID APP_ID = UUID.randomUUID();
    private static final String AUTH_HEADER = "Bearer test-token";
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    @BeforeEach
    void setUp() {
        var claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(USER_ID.toString());
        when(jwtTokenProvider.parseToken("test-token")).thenReturn(claims);
    }

    @Test
    @DisplayName("POST /api/applications/clock-fix - 打刻修正申請: 201")
    void submitClockFix_success_returns201() throws Exception {
        var request = new ClockFixRequest(
                LocalDate.now().minusDays(1),
                ZonedDateTime.now(TOKYO).withHour(9).withMinute(0),
                ZonedDateTime.now(TOKYO).withHour(18).withMinute(0),
                "打刻忘れ");

        var response = new ApplicationResponse(
                APP_ID, USER_ID, "テストユーザ", null,
                ApplicationType.CLOCK_FIX, ApplicationStatus.PENDING,
                null, ZonedDateTime.now(), null, null);
        when(applicationService.submitClockFix(eq(USER_ID), any(ClockFixRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/applications/clock-fix")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CLOCK_FIX"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/applications - 自分の申請一覧取得: 200")
    void getMyApplications_success_returns200() throws Exception {
        var response = new ApplicationResponse(
                APP_ID, USER_ID, "テストユーザ", null,
                ApplicationType.CLOCK_FIX, ApplicationStatus.PENDING,
                null, ZonedDateTime.now(), null, null);
        var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(applicationService.getMyApplications(eq(USER_ID), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/applications")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("CLOCK_FIX"));
    }

    @Test
    @DisplayName("POST /api/applications/{id}/approve - 承認: 200")
    void approve_success_returns200() throws Exception {
        var response = new ApplicationResponse(
                APP_ID, USER_ID, "テストユーザ", USER_ID,
                ApplicationType.CLOCK_FIX, ApplicationStatus.APPROVED,
                null, ZonedDateTime.now(), ZonedDateTime.now(), null);
        when(applicationService.approve(eq(APP_ID), eq(USER_ID))).thenReturn(response);

        mockMvc.perform(post("/api/applications/" + APP_ID + "/approve")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/applications/{id}/withdraw - 取り下げ: 200")
    void withdraw_success_returns200() throws Exception {
        var response = new ApplicationResponse(
                APP_ID, USER_ID, "テストユーザ", null,
                ApplicationType.CLOCK_FIX, ApplicationStatus.WITHDRAWN,
                null, ZonedDateTime.now(), ZonedDateTime.now(), null);
        when(applicationService.withdraw(eq(APP_ID), eq(USER_ID))).thenReturn(response);

        mockMvc.perform(post("/api/applications/" + APP_ID + "/withdraw")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
    }
}
