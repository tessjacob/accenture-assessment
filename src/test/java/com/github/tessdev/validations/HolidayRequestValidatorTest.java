package com.github.tessdev.validations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.tessdev.holidayservice.exception.InvalidCountException;
import com.github.tessdev.holidayservice.exception.InvalidCountryCodeException;
import com.github.tessdev.holidayservice.exception.InvalidYearException;
import com.github.tessdev.holidayservice.validation.HolidayRequestValidator;

public class HolidayRequestValidatorTest {
    private HolidayRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new HolidayRequestValidator();
    }

    /* ---------- Year Validation ---------- */
    @Test
    @DisplayName("validateYear: Valid years pass without exception.")
    void validateYearValidYearPasses() {
        int currentYear = java.time.Year.now().getValue();
        validator.validateYear(currentYear); // Should not throw
        validator.validateYear(2000); // Should not throw
    }

    @Test
    @DisplayName("validateYear: Year too early throws InvalidYearException.")
    void validateYearTooEarlyThrowsInvalidYearException() {
        int tooEarly = 1800;
        assertThatThrownBy(() -> validator.validateYear(tooEarly))
                .isInstanceOf(InvalidYearException.class)
                .hasMessageContaining("Invalid year"); // MIN_YEAR
    }

    @Test
    @DisplayName("validateYear: Future year throws InvalidYearException.")
    void validateYearFutureYearThrowsInvalidYearException() {
        int futureYear = java.time.Year.now().getValue() + 5;
        assertThatThrownBy(() -> validator.validateYear(futureYear))
                .isInstanceOf(InvalidYearException.class)
                .hasMessageContaining("Invalid year");
    }

    /* ---------- Country Codes Validation ---------- */
    @Test
    @DisplayName("validateCountryCodes: Valid country codes pass without exception.")
    void validateCountryCodesValidCodesPasses() {
        validator.validateCountryCodes(List.of("DE", "FR", "US")); // Should not throw
    }

    @Test
    @DisplayName("validateCountryCodes: null or empty list throws exception.")
    void validateCountryCodesNullOrEmptyThrowsException() {
        assertThatThrownBy(() -> validator.validateCountryCodes(null))
                .isInstanceOf(InvalidCountryCodeException.class)
                .hasMessageContaining("At least one country code");

        assertThatThrownBy(() -> validator.validateCountryCodes(List.of()))
                .isInstanceOf(InvalidCountryCodeException.class)
                .hasMessageContaining("At least one country code");
    }

    @Test
    @DisplayName("validateCountryCodes: Invalid country codes throw exception.")
    void validateCountryCodesInvalidCodesThrowsException() {
        List<String> invalid = Arrays.asList("DE", "X1", null, "USA");
        assertThatThrownBy(() -> validator.validateCountryCodes(invalid))
                .isInstanceOf(InvalidCountryCodeException.class)
                .hasMessageContaining("X1")
                .hasMessageContaining("null")
                .hasMessageContaining("USA");
    }

    @Test
    @DisplayName("validateCountryCode: Single valid code passes without exception.")
    void validateCountryCodeSingleValidCodePasses() {
        validator.validateCountryCode("NL"); // Should not throw
    }

    @Test
    @DisplayName("validateCountryCode: Single invalid code throws exception.")
    void validateCountryCodeSingleInvalidCodeThrowsException() {
        assertThatThrownBy(() -> validator.validateCountryCode("ZZZ"))
                .isInstanceOf(InvalidCountryCodeException.class)
                .hasMessageContaining("ZZZ");
    }

    /* ---------- Holiday Count Resolution ---------- */
    @Test
    @DisplayName("resolveCount: null or zero returns default count of 3.")
    void resolveCountNullOrZeroReturnsDefault() {
        assertEquals(3, validator.resolveCount(null));
        assertEquals(3, validator.resolveCount(0));
        assertEquals(3, validator.resolveCount(-5));
    }

    @Test
    @DisplayName("resolveCount: Valid counts within limits return same value.")
    void resolveCountWithinLimitsReturnsSame() {
        assertEquals(1, validator.resolveCount(1));
        assertEquals(2, validator.resolveCount(2));
        assertEquals(3, validator.resolveCount(3));
    }

    @Test
    @DisplayName("resolveCount: Count above limit throws InvalidCountException.")
    void resolveCountAboveLimitThrowsException() {
        assertThatThrownBy(() -> validator.resolveCount(4))
                .isInstanceOf(InvalidCountException.class)
                .hasMessageContaining("Count must be between 0 and 3");
    }
}
