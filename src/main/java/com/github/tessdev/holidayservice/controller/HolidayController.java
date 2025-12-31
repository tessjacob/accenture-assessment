package com.github.tessdev.holidayservice.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.service.HolidayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/holidays")
@Tag(name = "Public Holidays API")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping("/last/{country}")
    @Operation(summary = "Last 3 celebrated holidays for a country")
    public List<Holiday> lastThree(@PathVariable String country) {
        return holidayService.lastThreeHolidays(country);
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
