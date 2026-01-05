package com.github.tessdev.holidayservice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class InvalidYearException extends HolidayServiceException {
    public InvalidYearException(int year) {
        super(
                "INVALID_YEAR",
                "Invalid year.",
                HttpStatus.BAD_REQUEST,
                Map.of("year", year));
    }
}