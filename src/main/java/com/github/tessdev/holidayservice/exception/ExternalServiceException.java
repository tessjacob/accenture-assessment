package com.github.tessdev.holidayservice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends HolidayServiceException {
    public ExternalServiceException(String serviceUrl) {
        super(
                "SERVICE_UNAVAILABLE",
                "External service temporarily unavailable: " + serviceUrl,
                HttpStatus.SERVICE_UNAVAILABLE,
                Map.of("serviceUrl", serviceUrl));
    }
}