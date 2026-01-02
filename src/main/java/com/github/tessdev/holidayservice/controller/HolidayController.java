package com.github.tessdev.holidayservice.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.tessdev.holidayservice.exception.CountryNotSupportedException;
import com.github.tessdev.holidayservice.exception.InvalidCountryCodeException;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
import com.github.tessdev.holidayservice.service.HolidayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/holidays")
@Tag(name = "Public Holidays API")
public class HolidayController {

    private static final int DEFAULT_HOLIDAY_COUNT = 3;

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping("/last/{country}")
    @Operation(summary = "Last 3 celebrated holidays for a country")
    public LastHolidaysResponse getLastHolidays(
            @Parameter(description = "ISO 3166-1 alpha-2 country code", example = "NL") @PathVariable String country,
            @Parameter(description = "Maximum number of holidays to return (0–3)", example = "3") @RequestParam(value = "count", defaultValue = "3") Integer count)
            throws IOException, InterruptedException {

        validateCountryCode(country);
        int effectiveCount = resolveCount(count);

        List<Holiday> results = holidayService.getLastHolidays(
                country.toUpperCase(),
                effectiveCount);

        if (results.isEmpty()) {
            throw new CountryNotSupportedException(country);
        }

        return new LastHolidaysResponse(
                country.toUpperCase(),
                results,
                results.size());
    }

    private int resolveCount(Integer count) {
        if (count == null || count <= 0) {
            return DEFAULT_HOLIDAY_COUNT;
        }

        if (count > 3) {
            throw new IllegalArgumentException("Count must be between 0 and 3");
        }
        return count;
    }

    private void validateCountryCode(String country) {
        if (country == null || !country.matches("^[A-Za-z]{2}$")) {
            throw new InvalidCountryCodeException(country);
        }
    }

    @GetMapping("/non-weekday-counts")
    public Map<String, Long> getWeekdayCounts(@RequestParam int year, @RequestParam List<String> countries)
            throws IOException, InterruptedException {
        return holidayService.getWeekdayHolidayCounts(year, countries);
    }

    @GetMapping("/common")
    public List<Holiday> getCommon(@RequestParam int year,
            @RequestParam String country1,
            @RequestParam String country2) throws IOException, InterruptedException {
        return holidayService.getCommonHolidays(year, country1, country2);
    }
}
