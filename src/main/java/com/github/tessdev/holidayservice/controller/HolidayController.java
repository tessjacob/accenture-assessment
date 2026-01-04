package com.github.tessdev.holidayservice.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.tessdev.holidayservice.exception.CountryNotSupportedException;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
import com.github.tessdev.holidayservice.model.WeekdayHolidayCountsResponse;
import com.github.tessdev.holidayservice.service.HolidayService;
import com.github.tessdev.holidayservice.validation.HolidayRequestValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/holidays")
@Tag(name = "Public Holidays API")
public class HolidayController {

    private final HolidayService holidayService;

    private final HolidayRequestValidator validator;

    public HolidayController(HolidayService holidayService, HolidayRequestValidator validator) {
        this.holidayService = holidayService;
        this.validator = validator;
    }

    @GetMapping("/last/{country}")
    @Operation(summary = "Last 3 celebrated holidays for a country.")
    public ResponseEntity<LastHolidaysResponse> getLastHolidays(
            @Parameter(description = "ISO 3166-1 alpha-2 country code", example = "NL") @PathVariable String country,
            @Parameter(description = "Maximum number of holidays to return (0–3)", example = "3") @RequestParam(value = "count", defaultValue = "3") Integer count)
            throws IOException, InterruptedException {

        validator.validateCountryCode(country);
        int effectiveCount = validator.resolveCount(count);

        LastHolidaysResponse response = holidayService.getLastHolidays(country.toUpperCase(), effectiveCount);

        if (response.results().isEmpty()) {
            throw new CountryNotSupportedException(country);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekday-counts")
    @Operation(summary = "Get weekday holiday counts for multiple countries in a given year.")
    public ResponseEntity<WeekdayHolidayCountsResponse> getWeekdayCounts(
            @Parameter(description = "Year to get weekday holiday counts for", example = "2024") @RequestParam int year,
            @Parameter(description = "List of ISO 3166-1 alpha-2 country codes", example = "NL,DE") @RequestParam List<String> countries,
            @Parameter(description = "Include weekend holidays in the count", example = "true") @RequestParam(defaultValue = "true") boolean weekend,
            @Parameter(description = "Sort order of the results", example = "descending") @RequestParam(defaultValue = "descending") String sort)
            throws IOException, InterruptedException {

        validator.validateYear(year);
        validator.validateCountries(countries);
        validator.validateSort(sort);

        return ResponseEntity.ok(holidayService.getWeekdayHolidayCounts(year, countries, weekend, sort));
    }

    @GetMapping("/common")
    public List<Holiday> getCommon(@RequestParam int year,
            @RequestParam String country1,
            @RequestParam String country2) throws IOException, InterruptedException {
        return holidayService.getCommonHolidays(year, country1, country2);
    }
}
