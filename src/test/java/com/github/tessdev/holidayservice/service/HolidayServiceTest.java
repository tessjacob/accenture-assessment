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

import com.github.tessdev.holidayservice.model.CommonHoliday;
import com.github.tessdev.holidayservice.model.CommonHolidaysResponse;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.HolidayCountResult;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
import com.github.tessdev.holidayservice.model.SortOrder;
import com.github.tessdev.holidayservice.model.WeekdayHolidayCountsResponse;

@ExtendWith(MockitoExtension.class)
public class HolidayServiceTest {

        @Spy
        @InjectMocks
        private HolidayService holidayService;

        private static final int YEAR = 2024;
        private static final String DE = "DE";
        private static final String FR = "FR";

        @Test
        @DisplayName("Returns holidays from NagerApiClient.")
        void fetchHolidaysForYearReturnsClientResult() {
                List<Holiday> apiHolidays = List.of(new Holiday(LocalDate.of(2024, 1, 1), "New Year"));
                doReturn(apiHolidays).when(holidayService).fetchHolidaysForYear("DE", YEAR);

                List<Holiday> result = holidayService.fetchHolidaysForYear("DE", YEAR);

                assertThat(result).isEqualTo(apiHolidays);
        }

        @Test
        @DisplayName("Handles empty list.")
        void fetchHolidaysForYearEmptyList() {
                doReturn(List.of()).when(holidayService).fetchHolidaysForYear("DE", YEAR);

                List<Holiday> result = holidayService.fetchHolidaysForYear("DE", YEAR);

                assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return only celebrated holidays.")
        void shouldReturnOnlyCelebratedHolidays() {
                // given
                LocalDate today = LocalDate.now();

                List<Holiday> input = List.of(
                                new Holiday(today.minusDays(10), "Past 1"),
                                new Holiday(today.plusDays(5), "Future"),
                                new Holiday(today.minusDays(1), "Past 2"));

                doReturn(input)
                                .when(holidayService)
                                .fetchHolidaysForYear(eq("NL"), anyInt());

                // when
                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 3);

                // then
                assertEquals(2, response.results().size());
                assertTrue(response.results().stream().allMatch(h -> h.date().isBefore(today)));
                assertEquals(2, response.count());
                assertEquals("NL", response.country());
        }

        @Test
        @DisplayName("Should sort holidays by date descending.")
        void shouldSortHolidaysByDateDescending() {
                // given
                LocalDate today = LocalDate.now();

                Holiday older = new Holiday(today.minusDays(20), "Older");
                Holiday newer = new Holiday(today.minusDays(5), "Newer");

                doReturn(List.of(older, newer))
                                .when(holidayService)
                                .fetchHolidaysForYear(eq("NL"), anyInt());

                // when
                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 5);

                // then
                assertEquals("Newer", response.results().get(0).name());
                assertEquals("Older", response.results().get(1).name());
        }

        @Test
        @DisplayName("Should limit number of results.")
        void shouldLimitNumberOfResults() {
                // given
                LocalDate today = LocalDate.now();

                List<Holiday> input = List.of(
                                new Holiday(today.minusDays(1), "H1"),
                                new Holiday(today.minusDays(2), "H2"),
                                new Holiday(today.minusDays(3), "H3"));

                doReturn(input)
                                .when(holidayService)
                                .fetchHolidaysForYear(eq("NL"), anyInt());

                // when
                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 2);

                // then
                assertEquals(2, response.results().size());
                assertEquals(2, response.count());
        }

        @Test
        @DisplayName("Should return empty list when limit is zero.")
        void shouldReturnEmptyListWhenLimitIsZero() {
                // given
                LocalDate today = LocalDate.now();

                doReturn(List.of(
                                new Holiday(today.minusDays(1), "H1")))
                                .when(holidayService)
                                .fetchHolidaysForYear(eq("NL"), anyInt());

                // when
                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 0);

                // then
                assertTrue(response.results().isEmpty());
                assertEquals(0, response.count());
        }

        @Test
        @DisplayName("Should return empty list when no holidays exist.")
        void shouldReturnEmptyListWhenNoHolidaysExist() {
                // given
                doReturn(List.of())
                                .when(holidayService)
                                .fetchHolidaysForYear(eq("NL"), anyInt());

                // when
                LastHolidaysResponse response = holidayService.getLastHolidays("NL", 3);

                // then
                assertTrue(response.results().isEmpty());
                assertEquals(0, response.count());
        }

        @Test
        @DisplayName("Returns fewer results than limit when not enough holidays.")
        void getLastHolidaysFewerThanLimit() {
                LocalDate past = LocalDate.now().minusDays(1);
                doReturn(List.of(new Holiday(past, "Holiday 1")))
                                .when(holidayService)
                                .fetchHolidaysForYear("DE", 2026);

                LastHolidaysResponse response = holidayService.getLastHolidays("DE", 5);

                assertThat(response.results()).hasSize(1);
                assertThat(response.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Returns empty list if only future holidays.")
        void getLastHolidaysFutureOnly() {
                LocalDate future = LocalDate.now().plusDays(5);
                doReturn(List.of(new Holiday(future, "Future Holiday")))
                                .when(holidayService)
                                .fetchHolidaysForYear("DE", 2026);

                LastHolidaysResponse response = holidayService.getLastHolidays("DE", 3);

                assertThat(response.results()).isEmpty();
                assertThat(response.count()).isZero();
        }

        @Test
        @DisplayName("Should exclude weekend holidays when flag is true.")
        void excludesWeekendHolidaysWhenFlagIsTrue() {
                // given
                doReturn(holidays(
                                LocalDate.of(2024, 1, 1), // Monday
                                LocalDate.of(2024, 1, 6), // Saturday
                                LocalDate.of(2024, 1, 7) // Sunday
                )).when(holidayService).fetchHolidaysForYear("DE", YEAR);

                // when
                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE"), true, SortOrder.DESC);

                // then
                assertThat(response.results()).hasSize(1);
                assertThat(response.results().get(0).count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should include weekend holidays when flag is false.")
        void includesWeekendHolidaysWhenFlagIsFalse() {
                // given
                doReturn(holidays(
                                LocalDate.of(2024, 1, 6),
                                LocalDate.of(2024, 1, 7))).when(holidayService).fetchHolidaysForYear("DE", YEAR);

                // when
                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE"), false, SortOrder.DESC);

                // then
                assertThat(response.results().get(0).count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should sort results by count ascending.")
        void sortsResultsByCountAscending() {
                // given
                doReturn(holidays(LocalDate.of(2024, 1, 1)))
                                .when(holidayService).fetchHolidaysForYear("DE", YEAR);

                doReturn(holidays(
                                LocalDate.of(2024, 1, 1),
                                LocalDate.of(2024, 1, 2))).when(holidayService).fetchHolidaysForYear("FR", YEAR);

                // when
                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE", "FR"), true, SortOrder.DESC);

                // then
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
                // given
                doReturn(holidays(LocalDate.of(2024, 1, 1)))
                                .when(holidayService).fetchHolidaysForYear("DE", YEAR);

                doReturn(holidays(
                                LocalDate.of(2024, 1, 1),
                                LocalDate.of(2024, 1, 2))).when(holidayService).fetchHolidaysForYear("FR", YEAR);
                // when
                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE", "FR"), true, SortOrder.ASC);

                // then
                assertThat(response.results())
                                .extracting(HolidayCountResult::country)
                                .containsExactly("DE", "FR");
        }

        @Test
        @DisplayName("Country with no holidays returns zero count.")
        void countryWithNoHolidaysReturnsZero() {
                // given
                doReturn(List.of())
                                .when(holidayService).fetchHolidaysForYear("AQ", YEAR);

                // when
                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("AQ"), true, SortOrder.DESC);

                // then
                assertThat(response.results().get(0).count()).isZero();
        }

        @Test
        @DisplayName("Supports single country request.")
        void supportsSingleCountryRequest() {
                // given
                doReturn(holidays(LocalDate.of(2024, 12, 25)))
                                .when(holidayService).fetchHolidaysForYear("DE", YEAR);

                // when
                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE"), true, SortOrder.DESC);

                // then
                assertThat(response.results()).hasSize(1);
                assertThat(response.results().get(0).country()).isEqualTo("DE");
        }

        @Test
        @DisplayName("Handles multiple countries with varying holiday counts.")
        void getWeekdayHolidayCountsMultipleCountries() {
                doReturn(holidays(LocalDate.of(2024, 1, 1))).when(holidayService).fetchHolidaysForYear("DE", YEAR);
                doReturn(List.of()).when(holidayService).fetchHolidaysForYear("FR", YEAR);

                WeekdayHolidayCountsResponse response = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE", "FR"), true, SortOrder.DESC);

                assertThat(response.results())
                                .extracting(HolidayCountResult::count)
                                .containsExactly(1, 0);
        }

        @Test
        @DisplayName("Respects sorting.")
        void getWeekdayHolidayCountsSorting() {
                doReturn(holidays(LocalDate.of(2024, 1, 1))).when(holidayService).fetchHolidaysForYear("DE", YEAR);
                doReturn(holidays(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2))).when(holidayService)
                                .fetchHolidaysForYear("FR", YEAR);

                WeekdayHolidayCountsResponse asc = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE", "FR"), true, SortOrder.ASC);
                assertThat(asc.results())
                                .extracting(HolidayCountResult::country)
                                .containsExactly("DE", "FR");

                WeekdayHolidayCountsResponse desc = holidayService.getWeekdayHolidayCounts(
                                YEAR, List.of("DE", "FR"), true, SortOrder.DESC);
                assertThat(desc.results())
                                .extracting(HolidayCountResult::country)
                                .containsExactly("FR", "DE");
        }

        @Test
        @DisplayName("Should return common holidays between two countries.")
        void shouldReturnCommonHolidays() {
                // given
                LocalDate date = LocalDate.of(2024, 5, 1);

                List<Holiday> deHolidays = List.of(
                                new Holiday(date, "Tag der Arbeit"),
                                new Holiday(LocalDate.of(2024, 10, 3), "German Unity Day"));

                List<Holiday> frHolidays = List.of(
                                new Holiday(date, "Fête du Travail"));

                doReturn(deHolidays).when(holidayService).fetchHolidaysForYear(DE, YEAR);
                doReturn(frHolidays).when(holidayService).fetchHolidaysForYear(FR, YEAR);

                // when
                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, DE, FR);

                // then
                assertThat(response.year()).isEqualTo(YEAR);
                assertThat(response.results()).hasSize(1);

                CommonHoliday holiday = response.results().get(0);
                assertThat(holiday.date()).isEqualTo(date);
                assertThat(holiday.localNames())
                                .containsEntry(DE, "Tag der Arbeit")
                                .containsEntry(FR, "Fête du Travail");
        }

        @Test
        @DisplayName("Should return empty list when no common holidays exist.")
        void shouldReturnEmptyListWhenNoCommonHolidays() {
                // given
                doReturn(List.of(
                                new Holiday(LocalDate.of(2024, 1, 1), "Neujahr"))).when(holidayService)
                                .fetchHolidaysForYear(DE, YEAR);

                doReturn(List.of(
                                new Holiday(LocalDate.of(2024, 2, 2), "Chandeleur"))).when(holidayService)
                                .fetchHolidaysForYear(FR, YEAR);

                // when
                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, DE, FR);

                // then
                assertThat(response.results()).isEmpty();
        }

        @Test
        @DisplayName("Should deduplicate holidays by date.")
        void shouldDeduplicateHolidaysByDate() {
                // given
                LocalDate date = LocalDate.of(2024, 5, 1);

                doReturn(List.of(
                                new Holiday(date, "Tag der Arbeit"),
                                new Holiday(date, "Duplicate Entry"))).when(holidayService)
                                .fetchHolidaysForYear(DE, YEAR);

                doReturn(List.of(
                                new Holiday(date, "Fête du Travail"))).when(holidayService)
                                .fetchHolidaysForYear(FR, YEAR);

                // when
                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, DE, FR);

                // then
                assertThat(response.results()).hasSize(1);
        }

        @Test
        @DisplayName("Should sort results by date ascending.")
        void shouldSortResultsByDateAscending() {
                // given
                LocalDate may = LocalDate.of(2024, 5, 1);
                LocalDate jan = LocalDate.of(2024, 1, 1);

                doReturn(List.of(
                                new Holiday(may, "May Holiday"),
                                new Holiday(jan, "January Holiday"))).when(holidayService)
                                .fetchHolidaysForYear(DE, YEAR);

                doReturn(List.of(
                                new Holiday(may, "FR May"),
                                new Holiday(jan, "FR Jan"))).when(holidayService).fetchHolidaysForYear(FR, YEAR);
                // when
                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, DE, FR);

                // then
                assertThat(response.results())
                                .extracting(CommonHoliday::date)
                                .containsExactly(jan, may);
        }

        @Test
        @DisplayName("Should return all holidays when both countries are the same.")
        void shouldReturnAllHolidaysWhenBothCountriesAreTheSame() {
                // given
                LocalDate may = LocalDate.of(2024, 5, 1);
                LocalDate jan = LocalDate.of(2024, 1, 1);

                doReturn(List.of(
                                new Holiday(may, "May Holiday"),
                                new Holiday(jan, "January Holiday"))).when(holidayService)
                                .fetchHolidaysForYear(DE, YEAR);
                // when
                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, DE, DE);

                // then
                System.out.print(response.results().size());
        }

        @Test
        @DisplayName("Returns empty list if one country has no holidays.")
        void getCommonHolidaysOneCountryNoHolidays() {
                doReturn(List.of(new Holiday(LocalDate.of(2024, 1, 1), "Holiday DE")))
                                .when(holidayService).fetchHolidaysForYear("DE", YEAR);
                doReturn(List.of()).when(holidayService).fetchHolidaysForYear("FR", YEAR);

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "DE", "FR");

                assertThat(response.results()).isEmpty();
        }

        @Test
        @DisplayName("Handles multiple holidays on same date with different names.")
        void getCommonHolidaysDuplicateDatesDifferentNames() {
                LocalDate date = LocalDate.of(2024, 5, 1);

                doReturn(List.of(new Holiday(date, "DE Holiday 1"), new Holiday(date, "DE Holiday 2")))
                                .when(holidayService).fetchHolidaysForYear("DE", YEAR);
                doReturn(List.of(new Holiday(date, "FR Holiday")))
                                .when(holidayService).fetchHolidaysForYear("FR", YEAR);

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "DE", "FR");

                assertThat(response.results()).hasSize(1);
                assertThat(response.results().get(0).localNames())
                                .containsEntry("DE", "DE Holiday 1")
                                .containsEntry("FR", "FR Holiday");
        }

        @Test
        @DisplayName("Handles empty holiday lists gracefully.")
        void getCommonHolidaysEmptyLists() {
                doReturn(List.of()).when(holidayService).fetchHolidaysForYear("DE", YEAR);
                doReturn(List.of()).when(holidayService).fetchHolidaysForYear("FR", YEAR);

                CommonHolidaysResponse response = holidayService.getCommonHolidays(YEAR, "DE", "FR");

                assertThat(response.results()).isEmpty();
        }
}
