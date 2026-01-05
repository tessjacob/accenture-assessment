package com.github.tessdev.holidayservice.validation;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.tessdev.holidayservice.exception.InvalidCountException;
import com.github.tessdev.holidayservice.exception.InvalidCountryCodeException;
import com.github.tessdev.holidayservice.exception.InvalidYearException;

@Component
public class HolidayRequestValidator {

    private static final int DEFAULT_HOLIDAY_COUNT = 3;
    private static final int MIN_YEAR = 1900;

    public void validateYear(int year) {
        int currentYear = Year.now().getValue();

        if (year < MIN_YEAR) {
            throw new InvalidYearException(MIN_YEAR);
        }

        if (year > currentYear) {
            throw new InvalidYearException(currentYear);
        }
    }

    public void validateCountryCodes(List<String> countries) {
        if (countries == null || countries.isEmpty()) {
            throw new InvalidCountryCodeException("At least one country code is required.");
        }

        // Collect all invalid country codes, including null values
        List<String> invalidCountries = countries.stream()
                .map(c -> c == null ? "null" : c) // convert null to string
                .filter(c -> !c.matches("^[A-Z]{2}$"))
                .toList();

        if (!invalidCountries.isEmpty()) {
            // Join invalid codes into a single string
            String invalidCodes = String.join(", ", invalidCountries);
            throw new InvalidCountryCodeException(invalidCodes);
        }
    }

    public int resolveCount(Integer count) {
        if (count == null || count <= 0) {
            return DEFAULT_HOLIDAY_COUNT;
        }

        if (count > 3) {
            throw new InvalidCountException("Count must be between 0 and 3");
        }
        return count;
    }

    public void validateCountryCode(String country) {
        validateCountryCodes(List.of(country));
    }
}
