package com.github.tessdev.holidayservice.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.github.tessdev.holidayservice.client.NagerApiClient;
import com.github.tessdev.holidayservice.model.CommonHoliday;
import com.github.tessdev.holidayservice.model.CommonHolidaysResponse;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.HolidayCountResult;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
import com.github.tessdev.holidayservice.model.SortOrder;
import com.github.tessdev.holidayservice.model.WeekdayHolidayCountsResponse;

@Service
public class HolidayService {

    private final NagerApiClient nagerApiClient;

    public HolidayService(NagerApiClient nagerApiClient) {
        this.nagerApiClient = nagerApiClient;
    }

    public List<Holiday> fetchHolidaysForYear(String country, int year) {
        return nagerApiClient.getHolidays(year, country);
    }

    public LastHolidaysResponse getLastHolidays(final String country, int limit) {
        int year = LocalDate.now().getYear();
        List<Holiday> results = fetchHolidaysForYear(country, year).stream()
                .filter(h -> h.date().isBefore(LocalDate.now())) // celebrated
                .sorted(Comparator.comparing(Holiday::date).reversed())
                .limit(limit)
                .map(h -> new Holiday(h.date(), h.localName()))
                .toList();

        return new LastHolidaysResponse(
                country.toUpperCase(),
                results,
                results.size());
    }

    public WeekdayHolidayCountsResponse getWeekdayHolidayCounts(int year,
            List<String> countries,
            boolean excludeWeekend, SortOrder sort) {
        List<HolidayCountResult> results = countries.stream()
                .map(country -> {
                    int count = (int) fetchHolidaysForYear(country, year).stream()
                            .filter(h -> !excludeWeekend || !isWeekend(h.date()))
                            .count();

                    return new HolidayCountResult(country, count);
                })
                .sorted(sort.comparator())
                .toList();
        return new WeekdayHolidayCountsResponse(year, results);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    public CommonHolidaysResponse getCommonHolidays(int year, String country1, String country2) {
        List<Holiday> holidays1 = fetchHolidaysForYear(country1, year);

        List<Holiday> holidays2 = fetchHolidaysForYear(country2, year);

        // Map date -> holiday localName for country1
        Map<LocalDate, String> map1 = holidays1.stream()
                .collect(Collectors.toMap(
                        Holiday::date,
                        Holiday::localName,
                        (a, b) -> a));

        // Intersect with country2 holidays
        List<CommonHoliday> common = holidays2.stream()
                .filter(h -> map1.containsKey(h.date()))
                .map(h -> {
                    // Use HashMap to allow duplicate keys (same country)
                    Map<String, String> localNames = new HashMap<>();
                    localNames.put(country1, map1.get(h.date()));
                    localNames.put(country2, h.localName());
                    return new CommonHoliday(h.date(), localNames);
                })
                .distinct()
                .sorted(Comparator.comparing(CommonHoliday::date))
                .toList();

        return new CommonHolidaysResponse(year, common);
    }
}
