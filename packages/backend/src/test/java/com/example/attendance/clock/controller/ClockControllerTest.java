package com.example.attendance.clock.controller;

import com.example.attendance.clock.dto.ClockStatusResponse;
import com.example.attendance.clock.dto.TimeRecordResponse;
import com.example.attendance.clock.service.ClockService;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.GlobalExceptionHandler;
import com.example.attendance.infrastructure.security.JwtAuthFilter;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ClockController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}),
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@ActiveProfiles("test")
class ClockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClockService clockService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    @Test
    @DisplayName("POST /api/clock/in - 正常に出勤打刻: 201")
    void clockIn_success_returns201() throws Exception {
        var response = new TimeRecordResponse(
                UUID.randomUUID(), LocalDate.now(),
                ZonedDateTime.now(TOKYO), null,
                60, null, null, null, null, false);
        when(clockService.clockIn(any(UUID.class))).thenReturn(response);

        mockMvc.perform(post("/api/clock/in").param("userId", USER_ID.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workDate").exists())
                .andExpect(jsonPath("$.clockIn").exists());
    }

    @Test
    @DisplayName("POST /api/clock/in - 重複打刻: 409")
    void clockIn_duplicate_returns409() throws Exception {
        when(clockService.clockIn(any(UUID.class)))
                .thenThrow(new BusinessException("本日は既に出勤打刻済みです", HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/clock/in").param("userId", USER_ID.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("本日は既に出勤打刻済みです"));
    }

    @Test
    @DisplayName("POST /api/clock/out - 正常に退勤打刻: 200")
    void clockOut_success_returns200() throws Exception {
        var response = new TimeRecordResponse(
                UUID.randomUUID(), LocalDate.now(),
                ZonedDateTime.now(TOKYO).minusHours(8), ZonedDateTime.now(TOKYO),
                60, 420, 0, 0, 0, false);
        when(clockService.clockOut(any(UUID.class))).thenReturn(response);

        mockMvc.perform(post("/api/clock/out").param("userId", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOut").exists())
                .andExpect(jsonPath("$.workMinutes").exists());
    }

    @Test
    @DisplayName("GET /api/clock/status - 打刻状態取得: 200")
    void getStatus_success_returns200() throws Exception {
        var response = new ClockStatusResponse(
                LocalDate.now(), ZonedDateTime.now(TOKYO), null,
                "WORKING", List.of());
        when(clockService.getStatus(any(UUID.class))).thenReturn(response);

        mockMvc.perform(get("/api/clock/status").param("userId", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentState").value("WORKING"));
    }
}
