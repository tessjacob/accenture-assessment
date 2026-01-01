package com.github.tessdev.holidayservice.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.github.tessdev.holidayservice.client.NagerApiClient;
import com.github.tessdev.holidayservice.model.Holiday;

@Service
public class HolidayService {

    private final NagerApiClient nagerApiClient;

    public HolidayService(NagerApiClient nagerApiClient) {
        this.nagerApiClient = nagerApiClient;
    }

    public List<Holiday> fetchHolidaysForYear(String country, int year) {
        return nagerApiClient.getHolidays(year, country);
    }

    public List<Holiday> getLastHolidays(final String country,
            int limit) {
        int year = LocalDate.now().getYear();
        List<Holiday> holidays = fetchHolidaysForYear(country, year);
        return holidays.stream()
                .filter(h -> h.date().isBefore(LocalDate.now())) // celebrated
                .sorted(Comparator.comparing(Holiday::date).reversed())
                .limit(limit)
                .map(h -> new Holiday(h.date(), h.name()))
                .toList();
    }

    public Map<String, Long> getWeekdayHolidayCounts(int year, List<String> countries) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWeekdayHolidayCounts'");
    }

    public List<Holiday> getCommonHolidays(int year, String country1, String country2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCommonHolidays'");
    }
}
