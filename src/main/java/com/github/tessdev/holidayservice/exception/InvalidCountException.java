package com.github.tessdev.holidayservice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class InvalidCountException extends HolidayServiceException {

    public InvalidCountException(String count) {
        super(
                "INVALID_COUNT_EXCEPTION",
                "Count must be between 0 and 3",
                HttpStatus.BAD_REQUEST,
                Map.of("count", count));
    }

}
