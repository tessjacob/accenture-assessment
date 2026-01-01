package com.github.tessdev.holidayservice.exception;

public class InvalidCountryCodeException extends RuntimeException {

    private final String countryCode;

    public InvalidCountryCodeException(String countryCode) {
        super("Invalid country code: " + countryCode);
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
