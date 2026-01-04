package com.github.tessdev.holidayservice.validation;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.tessdev.holidayservice.exception.InvalidCountryCodeException;
import com.github.tessdev.holidayservice.exception.InvalidRequestException;
import com.github.tessdev.holidayservice.exception.InvalidYearException;

@Component
public class HolidayRequestValidator {

    private static final int DEFAULT_HOLIDAY_COUNT = 3;
    private static final int MIN_YEAR = 1900;

    public void validateYear(int year) {
        int currentYear = Year.now().getValue();

        if (year < MIN_YEAR) {
            throw new InvalidYearException("Year must be >= " + MIN_YEAR);
        }

        if (year > currentYear) {
            throw new InvalidYearException("Year cannot be in the future");
        }
    }

    public void validateCountries(List<String> countries) {
        if (countries == null || countries.isEmpty()) {
            throw new InvalidCountryCodeException("At least one country code must be provided");
        }

        boolean hasInvalid = countries.stream()
                .anyMatch(c -> c == null || !c.matches("^[A-Z]{2}$"));

        if (hasInvalid) {
            throw new InvalidCountryCodeException();
        }
    }

    public int resolveCount(Integer count) {
        if (count == null || count <= 0) {
            return DEFAULT_HOLIDAY_COUNT;
        }

        if (count > 3) {
            throw new InvalidRequestException("Count must be between 0 and 3");
        }
        return count;
    }

    public void validateCountryCode(String country) {
        if (country == null || !country.matches("^[A-Za-z]{2}$")) {
            throw new InvalidCountryCodeException(country);
        }
    }
}
