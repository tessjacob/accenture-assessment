package com.github.tessdev.holidayservice.exception;

public class CountryNotSupportedException extends RuntimeException {

    private final String countryCode;

    public CountryNotSupportedException(String countryCode) {
        super("Country not supported: " + countryCode);
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

}
