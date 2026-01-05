package com.github.tessdev.holidayservice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class CountryNotSupportedException extends HolidayServiceException {

    public CountryNotSupportedException(String countryCode) {
        super(
                "COUNTRY_NOT_SUPPORTED",
                "Country not supported: " + countryCode,
                HttpStatus.BAD_REQUEST,
                Map.of("countryCode", countryCode));
    }

}
