package com.github.tessdev.holidayservice.cucumber;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestContext {
    public String countryCode;
    public ResponseEntity<String> response;
    public int year;

    // Shared flags
    public boolean apiUnavailable = false;
    public boolean internalError = false;
}
