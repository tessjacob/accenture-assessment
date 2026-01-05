package com.github.tessdev.holidayservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.EnableCaching;

import com.github.tessdev.holidayservice.client.NagerApiClient;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.service.HolidayService;

@SpringBootTest
@EnableCaching
class HolidayServiceCacheTest {

    @Autowired
    private HolidayService holidayService;

    @MockBean
    private NagerApiClient nagerApiClient;

    @Test
    void fetchHolidaysForYear_isCached() {
        String country = "DE";
        int year = 2024;

        List<Holiday> holidays = List.of(new Holiday(LocalDate.of(2024, 1, 1), "Neujahr"));
        when(nagerApiClient.getHolidays(year, country)).thenReturn(holidays);

        // First call
        List<Holiday> call1 = holidayService.fetchHolidaysForYear(country, year);
        // Second call (should hit cache)
        List<Holiday> call2 = holidayService.fetchHolidaysForYear(country, year);

        verify(nagerApiClient, times(1)).getHolidays(year, country);
        assertEquals(call1, call2);
    }
}