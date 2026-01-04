package com.github.tessdev.holidayservice.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.tessdev.holidayservice.client.NagerApiClient;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.HolidayCountResult;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
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
                .map(h -> new Holiday(h.date(), h.name()))
                .toList();

        return new LastHolidaysResponse(
                country.toUpperCase(),
                results,
                results.size());
    }

    public WeekdayHolidayCountsResponse getWeekdayHolidayCounts(int year,
            List<String> countries,
            boolean excludeWeekend, String sort) {
        List<HolidayCountResult> results = countries.stream()
                .map(country -> {
                    int count = (int) fetchHolidaysForYear(country, year).stream()
                            .filter(h -> !excludeWeekend || !isWeekend(h.date()))
                            .count();

                    return new HolidayCountResult(country, count);
                })
                .sorted(getComparator(sort))
                .toList();
        return new WeekdayHolidayCountsResponse(year, results);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    private Comparator<HolidayCountResult> getComparator(String sort) {
        Comparator<HolidayCountResult> comparator = Comparator.comparingInt(HolidayCountResult::count);

        return "ascending".equalsIgnoreCase(sort)
                ? comparator
                : comparator.reversed();
    }

    public List<Holiday> getCommonHolidays(int year, String country1, String country2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCommonHolidays'");
    }
}
