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

import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
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

                // Create a LastHolidaysResponse with 3 holidays
                LastHolidaysResponse response = new LastHolidaysResponse(
                                "DE",
                                List.of(
                                                new Holiday("2024-10-03", "German Unity Day"),
                                                new Holiday("2024-12-25", "Christmas Day"),
                                                new Holiday("2024-01-01", "New Year's Day")),
                                3 // total count
                );

                // Mock the service to return the response
                when(holidayService.getLastHolidays("DE", 3)).thenReturn(response);

                // Perform GET request
                mockMvc.perform(get("/api/holidays/last/DE")
                                .param("count", "3"))
                                .andExpect(status().isOk());
        }
}
