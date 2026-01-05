package com.github.tessdev.holidayservice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class InvalidCountryCodeException extends HolidayServiceException {

    public InvalidCountryCodeException(String countryCode) {
        super(
                "INVALID_COUNTRY_CODE",
                "Country code must be a valid ISO 3166-1 alpha-2 code: " + countryCode,
                HttpStatus.BAD_REQUEST,
                Map.of("countryCode", countryCode) // example details
        );
    }
}
