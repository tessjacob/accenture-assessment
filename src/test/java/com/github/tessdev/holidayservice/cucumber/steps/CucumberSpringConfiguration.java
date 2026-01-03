package com.github.tessdev.holidayservice.cucumber.steps;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.github.tessdev.holidayservice.cucumber.CucumberTestConfig;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CucumberTestConfig.class)
public class CucumberSpringConfiguration {

}
