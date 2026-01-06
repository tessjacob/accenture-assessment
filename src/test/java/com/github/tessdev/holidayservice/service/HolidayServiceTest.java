package com.github.tessdev.holidayservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;

import com.github.tessdev.holidayservice.client.NagerApiClient;
import com.github.tessdev.holidayservice.model.CommonHoliday;
import com.github.tessdev.holidayservice.model.CommonHolidaysResponse;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.HolidayCountResult;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
import com.github.tessdev.holidayservice.model.SortOrder;
import com.github.tessdev.holidayservice.model.WeekdayHolidayCountsResponse;

@SpringBootTest
@EnableCaching
public class HolidayServiceTest {

        @Autowired
        private HolidayService holidayService;

        @MockBean
        private NagerApiClient nagerApiClient;

        @Autowired
        private CacheManager cacheManager;

        private static final int YEAR = 2024;

        @BeforeEach
        void clearCache() {
                cacheManager.getCache("holidays").clear();
        }

        private List<Holiday> holidays(LocalDate... dates) {
                return Arrays.stream(dates)
                                .map(date -> new Holiday(date, "Holiday on " + date))
                                .collect(Collectors.toList());
        }

        // ===== fetchHolidaysForYear Tests =====
        @Test
        @DisplayName("Returns holidays from NagerApiClient.")
        void fetchHolidaysForYearReturnsClientResult() {
                List<Holiday> apiHolidays = List.of(new Holiday(LocalDate.of(2024, 1, 1), "New Year"));
                when(nagerApiClient.getHolidays(YEAR, "DE")).thenReturn(apiHolidays);

                List<Holiday> result = holidayService.fetchHolidaysForYear("DE", YEAR);
                assertThat(result).isEqualTo(apiHolidays);
        }

        @Test
        @DisplayName("Handles empty list.")
        void fetchHolidaysForYearEmptyList() {
                when(nagerApiClient.getHolidays(YEAR, "AU")).thenReturn(List.of());

                List<Holiday> result = holidayService.fetchHolidaysForYear("AU", YEAR);
                assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should cache results of fetchHolidaysForYear.")
        void fetchHolidaysForYearShouldUseCache() {
                List<Holiday> holidays = List.of(new Holiday(LocalDate.of(2024, 1, 1), "Neujahr"));
                when(nagerApiClient.getHolidays(YEAR, "BA")).thenReturn(holidays);

                List<Holiday> firstCall = holidayService.fetchHolidaysForYear("BA", YEAR);
                List<Holiday> secondCall = holidayService.fetchHolidaysForYear("BA", YEAR);

                verify(nagerApiClient, times(1)).getHolidays(YEAR, "BA");
                assertEquals(firstCall, secondCall);
        }

        // ===== LastHolidays Tests =====

        @Test

        @DisplayName("Should return only celebrated holidays.")
        void shouldReturnOnlyCelebratedHolidays() {
                LocalDate today = LocalDate.now();
                List<Holiday> input = List.of(
                                new Holiday(today.minusDays(10), "Past 1"),
                                new Holiday(today.plusDays(5), "Future"),
                                new Holiday(today.minusDays(1), "Past 2"));
                when(nagerApiClient.getHolidays(anyInt(), eq("KE"))).thenReturn(input);

                LastHolidaysResponse response = holidayService.getLastHolidays("KE", 3);

                assertEquals(2, response.results().size());
                assertTrue(response.results().stream().allMatch(h -> h.date().isBefore(today)));
                assertEquals(2, response.count());
                assertEquals("KE", response.country());
        }

        @Test

        @DisplayName("Should sort holidays by date descending.")
        void shouldSortHolidaysByDateDescending() {
                LocalDate today = LocalDate.now();
                when(nagerApiClient.getHolidays(anyInt(), eq("BO")))
                                .thenReturn(List.of(
                                                new Holiday(today.minusDays(20), "Older"),
                                                new Holiday(today.minusDays(5), "Newer")));

                LastHolidaysResponse response = holidayService.getLastHolidays("BO", 5);
                assertEquals("Newer", response.results().get(0).localName());
                assertEquals("Older", response.results().get(1).localName());
        }

        @Test

        @DisplayName("Should limit number of results.")
        void shouldLimitNumberOfResults() {
                LocalDate today = LocalDate.now();
                when(nagerApiClient.getHolidays(anyInt(), eq("BW"))).thenReturn(List.of(
                                new Holiday(today.minusDays(1), "H1"),
                                new Holiday(today.minusDays(2), "H2"),
                                new Holiday(today.minusDays(3), "H3")));

                LastHolidaysResponse response = holidayService.getLastHolidays("BW", 2);
                assertEquals(2, response.results().size());
                assertEquals(2, response.count());
        }

        @Test
        @DisplayName("Should return empty list when limit is zero.")
        void shouldReturnEmptyListWhenLimitIsZero() {
                LocalDate today = LocalDate.now();
                when(nagerApiClient.getHolidays(YEAR, "CA")).thenReturn(List.of(
                                new Holiday(today.minusDays(1), "H1")));

                LastHolidaysResponse response = holidayService.getLastHolidays("CA", 0);
                assertTrue(response.results().isEmpty());
                assertEquals(0, response.count());
        }

        @Test
        @DisplayName("Returns fewer results than limit when not enough holidays.")
        void getLastHolidaysFewerThanLimit() {
                LocalDate past = LocalDate.now().minusDays(1);
                when(nagerApiClient.getHolidays(2026, "BS")).thenReturn(List.of(new Holiday(past, "Holiday 1")));

                LastHolidaysResponse response = holidayService.getLastHolidays("BS", 5);
                assertThat(response.results()).hasSize(1);
                assertThat(response.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Returns empty list if only future holidays.")
        void getLastHolidaysFutureOnly() {
                LocalDate future = LocalDate.now().plusDays(5);
                when(nagerApiClient.getHolidays(2026, "BJ")).thenReturn(List.of(new Holiday(future, "Future Holiday")));

                LastHolidaysResponse response = holidayService.getLastHolidays("BJ", 3);
                assertThat(response.results()).isEmpty();
                assertThat(response.count()).isZero();
        }

        // ===== WeekdayHolidayCounts Tests =====
        @Test
        @DisplayName("Should exclude weekend holidays when flag is true.")
        void excludesWeekendHolidaysWhenFlagIsTrue() {
                when(nagerApiClient.getHolidays(YEAR, "BB")).thenReturn(holidays(
                                LocalDate.of(2024, 1, 1), // Monday
                                LocalDate.of(2024, 1, 6), // Saturday
                                LocalDate.of(2024, 1, 7) // Sunday
                ));

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("BB"), true, SortOrder.DESC);

                assertThat(response.results()).hasSize(1);
                assertThat(response.results().get(0).count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should include weekend holidays when flag is false.")
        void includesWeekendHolidaysWhenFlagIsFalse() {
                when(nagerApiClient.getHolidays(YEAR, "AM")).thenReturn(holidays(
                                LocalDate.of(2024, 1, 6),
                                LocalDate.of(2024, 1, 7)));

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("AM"), false, SortOrder.DESC);

                assertThat(response.results().get(0).count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should sort results by count ascending.")
        void sortsResultsByCountAscending() {
                when(nagerApiClient.getHolidays(YEAR, "CD")).thenReturn(holidays(LocalDate.of(2024, 1, 1)));
                when(nagerApiClient.getHolidays(YEAR, "DK")).thenReturn(holidays(
                                LocalDate.of(2024, 1, 1),
                                LocalDate.of(2024, 1, 2)));

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("CD", "DK"), true, SortOrder.DESC);

                assertThat(response.results())
                                .extracting(HolidayCountResult::country)
                                .containsExactly("DK", "CD");
        }

        @Test
        @DisplayName("Should sort results by count ascending when requested.")
        void sortsResultsByCountAscendingWhenRequested() {
                when(nagerApiClient.getHolidays(YEAR, "DO")).thenReturn(holidays(LocalDate.of(2024, 1, 1)));
                when(nagerApiClient.getHolidays(YEAR, "EC")).thenReturn(holidays(
                                LocalDate.of(2024, 1, 1),
                                LocalDate.of(2024, 1, 2)));

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DO", "EC"), true, SortOrder.ASC);

                assertThat(response.results())
                                .extracting(HolidayCountResult::country)
                                .containsExactly("DO", "EC");
        }

        @Test
        @DisplayName("Country with no holidays returns zero count.")
        void countryWithNoHolidaysReturnsZero() {
                when(nagerApiClient.getHolidays(YEAR, "CZ")).thenReturn(List.of());

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("CZ"), true, SortOrder.DESC);

                assertThat(response.results().get(0).count()).isZero();
        }

        // ===== CommonHolidays Tests =====
        @Test
        @DisplayName("Should return common holidays between two countries.")
        void shouldReturnCommonHolidays() {
                LocalDate date = LocalDate.of(2024, 5, 1);

                when(nagerApiClient.getHolidays(YEAR, "FO")).thenReturn(List.of(
                                new Holiday(date, "Tag der Arbeit"),
                                new Holiday(LocalDate.of(2024, 10, 3), "German Unity Day")));

                when(nagerApiClient.getHolidays(YEAR, "FI")).thenReturn(List.of(
                                new Holiday(date, "Fête du Travail")));

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "FO", "FI");

                assertThat(response.year()).isEqualTo(YEAR);
                assertThat(response.results()).hasSize(1);
                CommonHoliday holiday = response.results().get(0);
                assertThat(holiday.date()).isEqualTo(date);
                assertThat(holiday.localNames())
                                .containsEntry("FO", "Tag der Arbeit")
                                .containsEntry("FI", "Fête du Travail");
        }

        @Test
        @DisplayName("Should return empty list when no common holidays exist.")
        void shouldReturnEmptyListWhenNoCommonHolidays() {
                when(nagerApiClient.getHolidays(YEAR, "EE"))
                                .thenReturn(List.of(new Holiday(LocalDate.of(2024, 1, 1), "Neujahr")));
                when(nagerApiClient.getHolidays(YEAR, "SV"))
                                .thenReturn(List.of(new Holiday(LocalDate.of(2024, 2, 2), "Chandeleur")));

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "EE", "SV");
                assertThat(response.results()).isEmpty();
        }

        @Test
        @DisplayName("Should deduplicate holidays by date.")
        void shouldDeduplicateHolidaysByDate() {
                LocalDate date = LocalDate.of(2024, 5, 1);
                when(nagerApiClient.getHolidays(YEAR, "GL")).thenReturn(List.of(
                                new Holiday(date, "Tag der Arbeit"),
                                new Holiday(date, "Duplicate Entry")));
                when(nagerApiClient.getHolidays(YEAR, "GD")).thenReturn(List.of(
                                new Holiday(date, "Fête du Travail")));

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "GL", "GD");
                assertThat(response.results()).hasSize(1);
        }

        @Test
        @DisplayName("Should sort results by date ascending.")
        void shouldSortResultsByDateAscending() {
                LocalDate may = LocalDate.of(2024, 5, 1);
                LocalDate jan = LocalDate.of(2024, 1, 1);
                when(nagerApiClient.getHolidays(YEAR, "IM")).thenReturn(List.of(
                                new Holiday(may, "May Holiday"),
                                new Holiday(jan, "January Holiday")));
                when(nagerApiClient.getHolidays(YEAR, "IT")).thenReturn(List.of(
                                new Holiday(may, "FR May"),
                                new Holiday(jan, "FR Jan")));

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "IM", "IT");
                assertThat(response.results())
                                .extracting(CommonHoliday::date)
                                .containsExactly(jan, may);
        }

        @Test
        @DisplayName("Should return all holidays when both countries are the same.")
        void shouldReturnAllHolidaysWhenBothCountriesAreTheSame() {
                LocalDate may = LocalDate.of(2024, 5, 1);
                LocalDate jan = LocalDate.of(2024, 1, 1);
                when(nagerApiClient.getHolidays(YEAR, "GY")).thenReturn(List.of(
                                new Holiday(may, "May Holiday"),
                                new Holiday(jan, "January Holiday")));

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "GY", "GY");
                assertThat(response.results().size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Handles empty holiday lists gracefully.")
        void getCommonHolidaysEmptyLists() {
                when(nagerApiClient.getHolidays(YEAR, "JM")).thenReturn(List.of());
                when(nagerApiClient.getHolidays(YEAR, "JP")).thenReturn(List.of());

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "JM", "JP");
                assertThat(response.results()).isEmpty();
        }

        @Test
        @DisplayName("Handles multiple holidays on same date with different names.")
        void getCommonHolidaysDuplicateDatesDifferentNames() {
                LocalDate date = LocalDate.of(2024, 5, 1);
                when(nagerApiClient.getHolidays(YEAR, "LV")).thenReturn(List.of(
                                new Holiday(date, "DE Holiday 1"),
                                new Holiday(date, "DE Holiday 2")));
                when(nagerApiClient.getHolidays(YEAR, "LS")).thenReturn(List.of(
                                new Holiday(date, "FR Holiday")));

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "LV", "LS");
                assertThat(response.results()).hasSize(1);
                assertThat(response.results().get(0).localNames())
                                .containsEntry("LV", "DE Holiday 1")
                                .containsEntry("LS", "FR Holiday");
        }
}
