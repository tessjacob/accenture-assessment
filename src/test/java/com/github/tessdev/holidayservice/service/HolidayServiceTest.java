package com.github.tessdev.holidayservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.HolidayCountResult;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
import com.github.tessdev.holidayservice.model.WeekdayHolidayCountsResponse;

@ExtendWith(MockitoExtension.class)
public class HolidayServiceTest {

        @Spy
        @InjectMocks
        private HolidayService holidayService;

        private static final int YEAR = 2024;

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

                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 3);

                assertEquals(2, response.results().size());
                assertTrue(response.results().stream().allMatch(h -> h.date().isBefore(today)));
                assertEquals(2, response.count());
                assertEquals("NL", response.country());
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

                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 5);

                assertEquals("Newer", response.results().get(0).name());
                assertEquals("Older", response.results().get(1).name());
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

                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 2);

                assertEquals(2, response.results().size());
                assertEquals(2, response.count());
        }

        @Test
        @DisplayName("Should return empty list when limit is zero.")
        void shouldReturnEmptyListWhenLimitIsZero() {
                LocalDate today = LocalDate.now();

                doReturn(List.of(
                                new Holiday(today.minusDays(1), "H1")))
                                .when(holidayService)
                                .fetchHolidaysForYear(eq("NL"), anyInt());

                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 0);

                assertTrue(response.results().isEmpty());
                assertEquals(0, response.count());
        }

        @Test
        @DisplayName("Should return empty list when no holidays exist.")
        void shouldReturnEmptyListWhenNoHolidaysExist() {
                doReturn(List.of())
                                .when(holidayService)
                                .fetchHolidaysForYear(eq("NL"), anyInt());

                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 3);

                assertTrue(response.results().isEmpty());
                assertEquals(0, response.count());
        }

        @Test
        @DisplayName("Should exclude weekend holidays when flag is true.")
        void excludesWeekendHolidaysWhenFlagIsTrue() {
                doReturn(holidays(
                                LocalDate.of(2024, 1, 1), // Monday
                                LocalDate.of(2024, 1, 6), // Saturday
                                LocalDate.of(2024, 1, 7) // Sunday
                )).when(holidayService).fetchHolidaysForYear("DE", YEAR);

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE"), true, "descending");

                assertThat(response.results()).hasSize(1);
                assertThat(response.results().get(0).count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should include weekend holidays when flag is false.")
        void includesWeekendHolidaysWhenFlagIsFalse() {
                doReturn(holidays(
                                LocalDate.of(2024, 1, 6),
                                LocalDate.of(2024, 1, 7))).when(holidayService).fetchHolidaysForYear("DE", YEAR);

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE"), false, "descending");

                assertThat(response.results().get(0).count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should sort results by count ascending.")
        void sortsResultsByCountAscending() {
                doReturn(holidays(LocalDate.of(2024, 1, 1)))
                                .when(holidayService).fetchHolidaysForYear("DE", YEAR);

                doReturn(holidays(
                                LocalDate.of(2024, 1, 1),
                                LocalDate.of(2024, 1, 2))).when(holidayService).fetchHolidaysForYear("FR", YEAR);

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE", "FR"), true, "descending");

                assertThat(response.results())
                                .extracting(HolidayCountResult::country)
                                .containsExactly("FR", "DE");
        }

        private List<Holiday> holidays(LocalDate... dates) {
                return Arrays.stream(dates)
                                .map(date -> new Holiday(date, "Holiday on " + date))
                                .collect(Collectors.toList());
        }

        @Test
        @DisplayName("Should sort results by count ascending when requested.")
        void sortsResultsByCountAscendingWhenRequested() {
                doReturn(holidays(LocalDate.of(2024, 1, 1)))
                                .when(holidayService).fetchHolidaysForYear("DE", YEAR);

                doReturn(holidays(
                                LocalDate.of(2024, 1, 1),
                                LocalDate.of(2024, 1, 2))).when(holidayService).fetchHolidaysForYear("FR", YEAR);

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE", "FR"), true, "ascending");

                assertThat(response.results())
                                .extracting(HolidayCountResult::country)
                                .containsExactly("DE", "FR");
        }

        @Test
        @DisplayName("Country with no holidays returns zero count.")
        void countryWithNoHolidaysReturnsZero() {
                doReturn(List.of())
                                .when(holidayService).fetchHolidaysForYear("AQ", YEAR);

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("AQ"), true, "descending");

                assertThat(response.results().get(0).count()).isZero();
        }

        @Test
        @DisplayName("Supports single country request.")
        void supportsSingleCountryRequest() {
                doReturn(holidays(LocalDate.of(2024, 12, 25)))
                                .when(holidayService).fetchHolidaysForYear("DE", YEAR);

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE"), true, "descending");

                assertThat(response.results()).hasSize(1);
                assertThat(response.results().get(0).country()).isEqualTo("DE");
        }
}
