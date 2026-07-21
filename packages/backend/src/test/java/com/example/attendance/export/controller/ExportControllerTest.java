package com.example.attendance.export.controller;

import com.example.attendance.config.SecurityConfig;
import com.example.attendance.export.service.CsvExportService;
import com.example.attendance.infrastructure.security.JwtAuthFilter;
import com.example.attendance.infrastructure.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ExportController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}),
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@ActiveProfiles("test")
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CsvExportService csvExportService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        var claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(USER_ID.toString());
        when(jwtTokenProvider.parseToken("test-token")).thenReturn(claims);
    }

    @Test
    @DisplayName("GET /api/export/time-records - CSV出力: 200")
    void exportTimeRecords_success_returnsCsv() throws Exception {
        var csvData = "日付,出勤,退勤\n2026-01-01,09:00,18:00\n".getBytes();
        when(csvExportService.exportTimeRecords(eq(USER_ID), eq(USER_ID), eq(2026), eq(1)))
                .thenReturn(csvData);

        mockMvc.perform(get("/api/export/time-records")
                        .header("Authorization", AUTH_HEADER)
                        .param("year", "2026")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"time_records_2026_1.csv\""));
    }

    @Test
    @DisplayName("GET /api/export/monthly-summary - 月別集計CSV出力: 200")
    void exportMonthlySummary_success_returnsCsv() throws Exception {
        var csvData = "社員名,出勤日数,労働時間\nテスト,20,160\n".getBytes();
        when(csvExportService.exportMonthlySummary(eq(USER_ID), eq(2026), eq(1)))
                .thenReturn(csvData);

        mockMvc.perform(get("/api/export/monthly-summary")
                        .header("Authorization", AUTH_HEADER)
                        .param("year", "2026")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"monthly_summary_2026_1.csv\""));
    }

    @Test
    @DisplayName("GET /api/export/users - ユーザCSV出力: 200")
    void exportUsers_success_returnsCsv() throws Exception {
        var csvData = "名前,メール,部署\nテスト,test@example.com,開発部\n".getBytes();
        when(csvExportService.exportUsers(eq(USER_ID))).thenReturn(csvData);

        mockMvc.perform(get("/api/export/users")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"users.csv\""));
    }
}
