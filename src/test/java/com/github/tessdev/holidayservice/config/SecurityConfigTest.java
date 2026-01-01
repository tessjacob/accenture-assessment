package com.github.tessdev.holidayservice.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.github.tessdev.holidayservice.service.HolidayService;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    HolidayService holidayService;

    @Test
    @DisplayName("Endpoint is accessible without authentication.")
    void endpointIsAccessibleWithoutAuth() throws Exception {
        when(holidayService.getLastHolidays("DE", 3))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/holidays/last/DE"))
                .andExpect(status().isOk());
    }
}
