package com.github.tessdev.holidayservice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class HolidayServiceException extends RuntimeException {

    private final String code; // Machine-readable error code
    private final HttpStatus status; // HTTP status to return
    private final Map<String, Object> details; // Optional structured details

    public HolidayServiceException(String code, String message, HttpStatus status) {
        this(code, message, status, null);
    }

    public HolidayServiceException(String code, String message, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
