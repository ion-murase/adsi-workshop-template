package com.example.attendance.clock.controller;

import com.example.attendance.clock.dto.HolidayResponse;
import com.example.attendance.clock.service.CalendarService;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.common.exception.GlobalExceptionHandler;
import com.example.attendance.infrastructure.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CalendarController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthFilter.class}),
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CalendarService calendarService;

    @Test
    @DisplayName("GET /api/calendar/holidays - 休日一覧取得: 200")
    void getHolidays_success_returns200() throws Exception {
        var holiday = new HolidayResponse(UUID.randomUUID(), LocalDate.of(2026, 8, 13), "お盆休み", 2026);
        when(calendarService.getHolidays(2026)).thenReturn(List.of(holiday));

        mockMvc.perform(get("/api/calendar/holidays").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].holidayName").value("お盆休み"));
    }

    @Test
    @DisplayName("POST /api/calendar/holidays - 休日登録: 201")
    void createHoliday_success_returns201() throws Exception {
        var date = LocalDate.of(2026, 8, 13);
        var response = new HolidayResponse(UUID.randomUUID(), date, "お盆休み", 2026);
        when(calendarService.createHoliday(date, "お盆休み")).thenReturn(response);

        var body = Map.of("holidayDate", "2026-08-13", "holidayName", "お盆休み");

        mockMvc.perform(post("/api/calendar/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holidayName").value("お盆休み"));
    }

    @Test
    @DisplayName("DELETE /api/calendar/holidays/{id} - 休日削除: 204")
    void deleteHoliday_success_returns204() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete("/api/calendar/holidays/" + id))
                .andExpect(status().isNoContent());

        verify(calendarService).deleteHoliday(id);
    }

    @Test
    @DisplayName("POST /api/calendar/holidays - バリデーションエラー: 400")
    void createHoliday_missingName_returns400() throws Exception {
        var body = Map.of("holidayDate", "2026-08-13", "holidayName", "");

        mockMvc.perform(post("/api/calendar/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
