package com.github.tessdev.holidayservice.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.http.HttpClient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HttpClientConfigTest {

    @Autowired
    HttpClient httpClient;

    @Test
    @DisplayName("HttpClient bean is created")
    void httpClientBeanExists() {
        assertNotNull(httpClient);
    }
}
