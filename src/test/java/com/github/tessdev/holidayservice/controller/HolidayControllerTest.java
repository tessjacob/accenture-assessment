package com.github.tessdev.holidayservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.tessdev.holidayservice.exception.InvalidCountryCodeException;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
import com.github.tessdev.holidayservice.service.HolidayService;

public class HolidayControllerTest {
    private HolidayService holidayService;
    private HolidayController holidayController;

    @BeforeEach
    void setup() {
        holidayService = mock(HolidayService.class);
        holidayController = new HolidayController(holidayService);
    }

    @Test
    @DisplayName("getLastHolidays with default count returns last 3 holidays.")
    void testGetLastHolidays_DefaultCount_ValidCountry() throws IOException, InterruptedException {
        var country = "NL";
        var holidays = List.of(
                new Holiday(LocalDate.of(2025, 5, 29), "Hemelvaartsdag"),
                new Holiday(LocalDate.of(2025, 5, 5), "Bevrijdingsdag"));

        when(holidayService.getLastHolidays(country, 3)).thenReturn(holidays);

        LastHolidaysResponse response = holidayController.getLastHolidays(country, null);

        assertEquals(country, response.country());
        assertEquals(2, response.count());
        assertEquals(holidays, response.results());
    }

    @Test
    @DisplayName("getLastHolidays with explicit count returns correct number of holidays.")
    void testGetLastHolidays_ExplicitCount() throws IOException, InterruptedException {
        var country = "NL";
        int count = 2;
        var holidays = List.of(
                new Holiday(LocalDate.of(2025, 5, 29), "Hemelvaartsdag"),
                new Holiday(LocalDate.of(2025, 5, 5), "Bevrijdingsdag"));

        when(holidayService.getLastHolidays(country, count)).thenReturn(holidays);

        LastHolidaysResponse response = holidayController.getLastHolidays(country, count);

        assertEquals(2, response.count());
        assertEquals(holidays, response.results());
    }

    @Test
    @DisplayName("getLastHolidays with lowercase country code converts to uppercase.")
    void testGetLastHolidays_LowercaseCountry() throws IOException, InterruptedException {
        var country = "nl";
        var holidays = List.of(
                new Holiday(LocalDate.of(2025, 5, 29), "Hemelvaartsdag"));

        when(holidayService.getLastHolidays("NL", 3)).thenReturn(holidays);

        LastHolidaysResponse response = holidayController.getLastHolidays(country, null);

        assertEquals("NL", response.country());
        assertEquals(1, response.count());
    }

    @Test
    @DisplayName("getLastHolidays with count greater than 3 throws IllegalArgumentException.")
    void testGetLastHolidays_CountGreaterThan3() {
        var country = "NL";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> holidayController.getLastHolidays(country, 5));

        assertEquals("Count must be between 0 and 3", ex.getMessage());
    }

    @Test
    @DisplayName("getLastHolidays with negative count defaults to 3 holidays.")
    void testGetLastHolidays_NegativeCount() throws IOException, InterruptedException {
        var country = "NL";
        var holidays = List.of(
                new Holiday(LocalDate.of(2025, 5, 29), "Hemelvaartsdag"),
                new Holiday(LocalDate.of(2025, 5, 5), "Bevrijdingsdag"));

        when(holidayService.getLastHolidays(country, 3)).thenReturn(holidays);

        LastHolidaysResponse response = holidayController.getLastHolidays(country, -1);

        assertEquals(country, response.country());
        assertEquals(2, response.count());
        assertEquals(holidays, response.results());
    }

    @Test
    @DisplayName("getLastHolidays with invalid country code throws InvalidCountryCodeException.")
    void testGetLastHolidays_InvalidCountryCode() {
        var invalidCountry = "XXX";

        InvalidCountryCodeException ex = assertThrows(
                InvalidCountryCodeException.class,
                () -> holidayController.getLastHolidays(invalidCountry, null));

        assertEquals(invalidCountry, ex.getCountryCode());
    }
}
