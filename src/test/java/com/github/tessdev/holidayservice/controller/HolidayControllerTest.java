package com.github.tessdev.holidayservice.controller;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.github.tessdev.holidayservice.exception.ExternalServiceException;
import com.github.tessdev.holidayservice.exception.GlobalExceptionHandler;
import com.github.tessdev.holidayservice.exception.InvalidCountryCodeException;
import com.github.tessdev.holidayservice.exception.InvalidRequestException;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.HolidayCountResult;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
import com.github.tessdev.holidayservice.model.WeekdayHolidayCountsResponse;
import com.github.tessdev.holidayservice.service.HolidayService;
import com.github.tessdev.holidayservice.validation.HolidayRequestValidator;

@WebMvcTest(controllers = HolidayController.class, excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
})
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "debug=true")
public class HolidayControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private HolidayService holidayService;

        @MockBean
        private HolidayRequestValidator validator;

        @Test
        @DisplayName("Should return last three holidays with default count.")
        void shouldReturnLastThreeHolidaysWithDefaultCount() throws Exception {

                LastHolidaysResponse response = new LastHolidaysResponse(
                                "NL",
                                List.of(
                                                new Holiday(LocalDate.parse("2024-12-25"), "Christmas Day"),
                                                new Holiday(LocalDate.parse("2024-04-01"), "Easter Monday"),
                                                new Holiday(LocalDate.parse("2024-03-29"), "Good Friday")),
                                3);

                when(validator.resolveCount(3)).thenReturn(3);
                when(holidayService.getLastHolidays("NL", 3)).thenReturn(response);

                mockMvc.perform(get("/api/holidays/last/{country}", "NL"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.country").value("NL"))
                                .andExpect(jsonPath("$.count").value(3))
                                .andExpect(jsonPath("$.results").isArray())
                                .andExpect(jsonPath("$.results.length()").value(3));

                verify(validator).validateCountryCode("NL");
                verify(validator).resolveCount(3);
                verify(holidayService).getLastHolidays("NL", 3);
        }

        @Test
        @DisplayName("Should return last two holidays when count is two.")
        void shouldReturnLastTwoHolidaysWhenCountIsTwo() throws Exception {

                LastHolidaysResponse response = new LastHolidaysResponse(
                                "DE",
                                List.of(
                                                new Holiday(LocalDate.parse("2024-12-25"), "Christmas Day"),
                                                new Holiday(LocalDate.parse("2024-10-03"), "German Unity Day")),
                                2);

                when(validator.resolveCount(2)).thenReturn(2);
                when(holidayService.getLastHolidays("DE", 2)).thenReturn(response);

                mockMvc.perform(get("/api/holidays/last/{country}", "DE")
                                .param("count", "2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.country").value("DE"))
                                .andExpect(jsonPath("$.count").value(2))
                                .andExpect(jsonPath("$.results.length()").value(2))
                                .andExpect(jsonPath("$.results[0].date").value("2024-12-25"))
                                .andExpect(jsonPath("$.results[1].date").value("2024-10-03"));

                verify(holidayService).getLastHolidays("DE", 2);
        }

        @Test
        @DisplayName("Should uppercase country code before calling service.")
        void shouldUppercaseCountryBeforeCallingService() throws Exception {
                LastHolidaysResponse response = new LastHolidaysResponse(
                                "FR",
                                List.of("2024-07-14"));

                when(validator.resolveCount(1)).thenReturn(1);
                when(holidayService.getLastHolidays("FR", 1)).thenReturn(response);

                mockMvc.perform(get("/api/holidays/last/{country}", "fr")
                                .param("count", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.country").value("FR"));

                verify(validator).validateCountryCode("fr");
                verify(holidayService).getLastHolidays("FR", 1);
        }

        @Test
        @DisplayName("Should return 400 when country code is invalid.")
        void shouldReturn400WhenCountryCodeIsInvalid() throws Exception {
                doThrow(new InvalidCountryCodeException("Invalid country code"))
                                .when(validator).validateCountryCode("XXX");

                mockMvc.perform(get("/api/holidays/last/{country}", "XXX"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when count is invalid.")
        void shouldReturn400WhenCountIsInvalid() throws Exception {
                doThrow(new InvalidRequestException("Invalid count"))
                                .when(validator).resolveCount(5);

                mockMvc.perform(get("/api/holidays/last/{country}", "NL")
                                .param("count", "5"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return weekday holiday counts for multiple countries.")
        void shouldReturnsWeekdayCountsForMultipleCountries() throws Exception {
                WeekdayHolidayCountsResponse response = new WeekdayHolidayCountsResponse(
                                2024,
                                List.of(
                                                new HolidayCountResult("DE", 10),
                                                new HolidayCountResult("FR", 8)));

                when(holidayService.getWeekdayHolidayCounts(
                                eq(2024),
                                eq(List.of("DE", "FR")),
                                eq(true),
                                eq("descending"))).thenReturn(response);

                mockMvc.perform(get("/api/holidays/weekday-counts")
                                .param("year", "2024")
                                .param("countries", "DE", "FR"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.year").value(2024))
                                .andExpect(jsonPath("$.results").isArray())
                                .andExpect(jsonPath("$.results.length()").value(2))
                                .andExpect(jsonPath("$.results[0].country").value("DE"))
                                .andExpect(jsonPath("$.results[0].count").value(10));
        }

        @Test
        @DisplayName("Applies default query parameters when not provided.")
        void appliesDefaultQueryParameters() throws Exception {
                when(holidayService.getWeekdayHolidayCounts(
                                anyInt(), anyList(), eq(true), eq("descending")))
                                .thenReturn(new WeekdayHolidayCountsResponse(2024, List.of()));

                mockMvc.perform(get("/api/holidays/weekday-counts")
                                .param("year", "2024")
                                .param("countries", "DE"))
                                .andExpect(status().isOk());

                verify(holidayService).getWeekdayHolidayCounts(
                                2024, List.of("DE"), true, "descending");
        }

        @Test
        @DisplayName("Supports custom sort and weekend flags.")
        void supportsCustomSortAndWeekendFlags() throws Exception {
                when(holidayService.getWeekdayHolidayCounts(
                                2024, List.of("DE"), false, "ascending"))
                                .thenReturn(new WeekdayHolidayCountsResponse(2024, List.of()));

                mockMvc.perform(get("/api/holidays/weekday-counts")
                                .param("year", "2024")
                                .param("countries", "DE")
                                .param("weekend", "false")
                                .param("sort", "ascending"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 400 for invalid country code.")
        void shouldReturns400ForInvalidCountryCode() throws Exception {
                doThrow(new InvalidCountryCodeException())
                                .when(validator).validateCountries(List.of("X1"));

                mockMvc.perform(get("/api/holidays/weekday-counts")
                                .param("year", "2024")
                                .param("countries", "X1"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for future year.")
        void shouldReturns400ForFutureYear() throws Exception {
                doThrow(new InvalidRequestException("Year cannot be in the future"))
                                .when(validator).validateYear(2100);

                mockMvc.perform(get("/api/holidays/weekday-counts")
                                .param("year", "2100")
                                .param("countries", "DE"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 503 when external service is unavailable.")
        void shouldReturns503WhenExternalServiceIsUnavailable() throws Exception {
                when(holidayService.getWeekdayHolidayCounts(
                                anyInt(), anyList(), anyBoolean(), anyString()))
                                .thenThrow(new ExternalServiceException("Nager API down"));

                mockMvc.perform(get("/api/holidays/weekday-counts")
                                .param("year", "2024")
                                .param("countries", "DE"))
                                .andExpect(status().isServiceUnavailable());
        }

        @Test
        @DisplayName("Should return 500 for unexpected exception.")
        void shouldReturns500ForUnexpectedException() throws Exception {
                when(holidayService.getWeekdayHolidayCounts(
                                anyInt(), anyList(), anyBoolean(), anyString()))
                                .thenThrow(new RuntimeException("Boom"));

                mockMvc.perform(get("/api/holidays/weekday-counts")
                                .param("year", "2024")
                                .param("countries", "DE"))
                                .andExpect(status().isInternalServerError());
        }
}
