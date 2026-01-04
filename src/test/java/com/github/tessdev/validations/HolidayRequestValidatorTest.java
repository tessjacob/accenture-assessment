package com.github.tessdev.validations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.tessdev.holidayservice.exception.InvalidCountryCodeException;
import com.github.tessdev.holidayservice.exception.InvalidYearException;
import com.github.tessdev.holidayservice.validation.HolidayRequestValidator;

public class HolidayRequestValidatorTest {
    private final HolidayRequestValidator validator = new HolidayRequestValidator();

    @Test
    void futureYear_shouldFail() {
        Throwable thrown = catchThrowable(() -> validator.validateYear(LocalDate.now().getYear() + 1));
        assertThat(thrown).isInstanceOf(InvalidYearException.class)
                .hasMessageContaining("Year cannot be in the future");
    }

    @Test
    void invalidCountry_shouldFail() {
        assertThatThrownBy(() -> validator.validateCountries(List.of("DE", "X1")))
                .isInstanceOf(InvalidCountryCodeException.class);
    }
}
