package com.example.attendance.clock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@Sql("/test-data-clock.sql")
class ClockIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String USER_ID = "00000000-0000-0000-0000-000000000010";

    @Test
    @DisplayName("出勤→退勤→月次履歴の一連フロー")
    void clockInOutAndViewRecords() throws Exception {
        mockMvc.perform(post("/api/clock/in").param("userId", USER_ID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clockIn").exists())
                .andExpect(jsonPath("$.clockOut").doesNotExist());

        mockMvc.perform(post("/api/clock/out").param("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOut").exists())
                .andExpect(jsonPath("$.workMinutes").isNumber());

        var today = LocalDate.now();
        mockMvc.perform(get("/api/time-records")
                        .param("userId", USER_ID)
                        .param("year", String.valueOf(today.getYear()))
                        .param("month", String.valueOf(today.getMonthValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workDate").exists())
                .andExpect(jsonPath("$[0].workMinutes").isNumber());

        mockMvc.perform(get("/api/time-records/summary")
                        .param("userId", USER_ID)
                        .param("year", String.valueOf(today.getYear()))
                        .param("month", String.valueOf(today.getMonthValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workDays").value(1));
    }

    @Test
    @DisplayName("出勤打刻の重複は409エラー")
    void clockIn_duplicate_returns409() throws Exception {
        mockMvc.perform(post("/api/clock/in").param("userId", USER_ID))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clock/in").param("userId", USER_ID))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("打刻状態の取得")
    void getStatus_afterClockIn_returnsWorking() throws Exception {
        mockMvc.perform(post("/api/clock/in").param("userId", USER_ID))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/clock/status").param("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentState").value("WORKING"));
    }

    @Test
    @DisplayName("外出→戻りの打刻フロー")
    void goOutAndReturn() throws Exception {
        mockMvc.perform(post("/api/clock/in").param("userId", USER_ID))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clock/go-out").param("userId", USER_ID))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/clock/status").param("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentState").value("OUT"));

        mockMvc.perform(post("/api/clock/return").param("userId", USER_ID))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/clock/status").param("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentState").value("WORKING"));
    }
}
