package com.github.tessdev.holidayservice.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tessdev.holidayservice.HolidayServiceApplication;
import com.github.tessdev.holidayservice.model.Holiday;

@SpringBootTest(classes = HolidayServiceApplication.class)
class JacksonConfigTest {

    @Autowired
    ObjectMapper mapper;

    @Test
    @DisplayName("ObjectMapper ignores unknown properties.")
    void ignoresUnknownProperties() throws Exception {
        String json = """
                    { "date":"2024-01-01", "name":"Test", "extra":"ignored" }
                """;

        Holiday h = mapper.readValue(json, Holiday.class);
        assertEquals("Test", h.name());
    }
}
