package com.github.tessdev.validations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.tessdev.holidayservice.exception.InvalidCountryCodeException;
import com.github.tessdev.holidayservice.exception.InvalidRequestException;
import com.github.tessdev.holidayservice.validation.HolidayRequestValidator;

public class HolidayRequestValidatorTest {
    private final HolidayRequestValidator validator = new HolidayRequestValidator();

    @Test
    void futureYear_shouldFail() {
        assertThatThrownBy(() -> validator.validateYear(3000))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void invalidCountry_shouldFail() {
        assertThatThrownBy(() -> validator.validateCountries(List.of("DE", "X1")))
                .isInstanceOf(InvalidCountryCodeException.class);
    }
}
