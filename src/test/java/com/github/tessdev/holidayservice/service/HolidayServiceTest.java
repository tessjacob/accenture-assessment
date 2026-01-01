package com.github.tessdev.holidayservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.tessdev.holidayservice.model.Holiday;

@ExtendWith(MockitoExtension.class)
public class HolidayServiceTest {

    @Spy
    @InjectMocks
    private HolidayService holidayService;

    @Test
    @DisplayName("Should return only celebrated holidays.")
    void shouldReturnOnlyCelebratedHolidays() {
        LocalDate today = LocalDate.now();

        List<Holiday> input = List.of(
                new Holiday(today.minusDays(10), "Past 1"),
                new Holiday(today.plusDays(5), "Future"),
                new Holiday(today.minusDays(1), "Past 2"));

        doReturn(input)
                .when(holidayService)
                .fetchHolidaysForYear(eq("NL"), anyInt());

        List<Holiday> result = holidayService.getLastHolidays("NL", 3);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(h -> h.date().isBefore(today)));
    }

    @Test
    @DisplayName("Should sort holidays by date descending.")
    void shouldSortHolidaysByDateDescending() {
        LocalDate today = LocalDate.now();

        Holiday older = new Holiday(today.minusDays(20), "Older");
        Holiday newer = new Holiday(today.minusDays(5), "Newer");

        doReturn(List.of(older, newer))
                .when(holidayService)
                .fetchHolidaysForYear(eq("NL"), anyInt());

        List<Holiday> result = holidayService.getLastHolidays("NL", 5);

        assertEquals("Newer", result.get(0).name());
        assertEquals("Older", result.get(1).name());
    }

    @Test
    @DisplayName("Should limit number of results.")
    void shouldLimitNumberOfResults() {
        LocalDate today = LocalDate.now();

        List<Holiday> input = List.of(
                new Holiday(today.minusDays(1), "H1"),
                new Holiday(today.minusDays(2), "H2"),
                new Holiday(today.minusDays(3), "H3"));

        doReturn(input)
                .when(holidayService)
                .fetchHolidaysForYear(eq("NL"), anyInt());

        List<Holiday> result = holidayService.getLastHolidays("NL", 2);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when limit is zero.")
    void shouldReturnEmptyListWhenLimitIsZero() {
        LocalDate today = LocalDate.now();

        doReturn(List.of(
                new Holiday(today.minusDays(1), "H1"))).when(holidayService)
                .fetchHolidaysForYear(eq("NL"), anyInt());

        List<Holiday> result = holidayService.getLastHolidays("NL", 0);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when no holidays exist.")
    void shouldReturnEmptyListWhenNoHolidaysExist() {
        doReturn(List.of())
                .when(holidayService)
                .fetchHolidaysForYear(eq("NL"), anyInt());

        List<Holiday> result = holidayService.getLastHolidays("NL", 3);

        assertTrue(result.isEmpty());
    }
}
