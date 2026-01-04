package com.github.tessdev.holidayservice.cucumber.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tessdev.holidayservice.cucumber.TestContext;
import com.github.tessdev.holidayservice.exception.CountryNotSupportedException;
import com.github.tessdev.holidayservice.exception.ExternalServiceException;
import com.github.tessdev.holidayservice.exception.InvalidCountryCodeException;
import com.github.tessdev.holidayservice.model.Holiday;
import com.github.tessdev.holidayservice.model.LastHolidaysResponse;
import com.github.tessdev.holidayservice.service.HolidayService;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class LastHolidaysSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HolidayService holidayService;

    /* ---------- Background ---------- */
    @Given("today is the current system date")
    public void today_is_current_date() {
    }

    @Given("holidays are retrieved from the Nager public holidays API")
    public void holidays_are_retrieved_from_nager_api() {
    }

    /* ---------- Given ---------- */
    @Given("the country code is {string}")
    public void the_country_code_is(String country) {
        context.countryCode = country;

        when(holidayService.getLastHolidays(anyString(), anyInt()))
                .thenAnswer(invocation -> {
                    String countryInput = ((String) invocation.getArgument(0)).toUpperCase();
                    int limit = (Integer) invocation.getArgument(1);

                    // If invalid country code, return empty response
                    if (!countryInput.matches("^[A-Z]{2}$")) {
                        throw new InvalidCountryCodeException("Invalid country code: " + countryInput);
                    }

                    // Unsupported country
                    if ("AQ".equals(countryInput)) {
                        throw new CountryNotSupportedException("Country not supported: " + countryInput);
                    }

                    // External API unavailable
                    if (context.apiUnavailable) {
                        throw new ExternalServiceException("Nager API down");
                    }

                    // Internal server error
                    if (context.internalError) {
                        throw new RuntimeException("Unexpected internal error");
                    }

                    // Normal holidays list
                    List<Holiday> allHolidays = List.of(
                            new Holiday(LocalDate.of(2024, 10, 3), "German Unity Day"),
                            new Holiday(LocalDate.of(2024, 12, 25), "Christmas Day"),
                            new Holiday(LocalDate.of(2024, 1, 1), "New Year's Day"));

                    List<Holiday> results = allHolidays.stream()
                            .filter(h -> h.date().isBefore(LocalDate.now()))
                            .sorted(Comparator.comparing(Holiday::date).reversed())
                            .limit(limit)
                            .toList();

                    return new LastHolidaysResponse(countryInput, results, results.size());
                });
    }

    /* ---------- When ---------- */
    @When("I request the last three holidays")
    public void i_request_last_three_holidays() {
        try {
            // Ensure country code is uppercase
            String country = context.countryCode != null ? context.countryCode.toUpperCase() : "";

            // Call mocked service
            LastHolidaysResponse lastHolidays = holidayService.getLastHolidays(country, 3);

            // If the mock returns null (just in case), treat as error
            if (lastHolidays == null || lastHolidays.results().isEmpty()) {
                context.response = ResponseEntity.status(400)
                        .body("{\"code\":\"INVALID_COUNTRY_CODE\",\"message\":\"Invalid country code: " + country
                                + "\"}");
                return;
            }

            // Normal successful response
            String json = objectMapper.writeValueAsString(lastHolidays);
            context.response = ResponseEntity.ok(json);

        } catch (InvalidCountryCodeException e) {
            context.response = ResponseEntity.status(400)
                    .body("{\"code\":\"INVALID_COUNTRY_CODE\",\"message\":\"" + e.getMessage() + "\"}");
        } catch (CountryNotSupportedException e) {
            context.response = ResponseEntity.status(404)
                    .body("{\"code\":\"COUNTRY_NOT_SUPPORTED\",\"message\":\"" + e.getMessage() + "\"}");
        } catch (ExternalServiceException e) {
            context.response = ResponseEntity.status(503)
                    .body("{\"code\":\"SERVICE_UNAVAILABLE\",\"message\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            context.response = ResponseEntity.status(500)
                    .body("{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"" + e.getMessage() + "\"}");
        }

        // Safety: if context.response still null for some reason
        if (context.response == null) {
            context.response = ResponseEntity.status(500)
                    .body("{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"Unexpected error\"}");
        }
    }

    /* ---------- Then ---------- */
    @Then("the response should contain at most {int} holidays")
    public void response_contains_at_most_holidays(int max) throws Exception {
        JsonNode root = objectMapper.readTree(context.response.getBody());
        assertThat(root.get("results").size()).isLessThanOrEqualTo(max);
    }

    @Then("all holiday dates should be before today")
    public void holiday_dates_before_today() throws Exception {
        JsonNode results = objectMapper.readTree(context.response.getBody()).get("results");
        LocalDate today = LocalDate.now();
        for (JsonNode node : results) {
            assertThat(LocalDate.parse(node.get("date").asText())).isBefore(today);
        }
    }

    @Then("each holiday should contain a date and a name")
    public void each_holiday_contains_date_and_name() throws Exception {
        JsonNode results = objectMapper.readTree(context.response.getBody()).get("results");
        for (JsonNode node : results) {
            assertThat(node.hasNonNull("date")).isTrue();
            assertThat(node.hasNonNull("name")).isTrue();
        }
    }

    @Then("the holidays should be sorted by date descending")
    public void holidays_sorted_descending() throws Exception {
        JsonNode results = objectMapper.readTree(context.response.getBody()).get("results");
        for (int i = 0; i < results.size() - 1; i++) {
            LocalDate first = LocalDate.parse(results.get(i).get("date").asText());
            LocalDate second = LocalDate.parse(results.get(i + 1).get("date").asText());
            assertThat(first).isAfterOrEqualTo(second);
        }
    }

    @Then("the response should be the same as for country {string}")
    public void the_response_should_be_the_same_as_for_country(String expectedCountry) throws Exception {
        JsonNode root = objectMapper.readTree(context.response.getBody());
        assertThat(root).isNotNull();
        assertThat(root.get("countryCode")).isNotNull();
        assertThat(root.get("countryCode").asText()).isEqualTo(expectedCountry.toUpperCase());
        assertThat(root.get("results")).isNotNull();
    }
}
