package com.github.tessdev.holidayservice.cucumber;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.github.tessdev.holidayservice.service.HolidayService;

@TestConfiguration
public class CucumberTestConfig {
    @MockBean
    HolidayService holidayService;

}
