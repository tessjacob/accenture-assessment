package com.github.tessdev.holidayservice.cucumber.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tessdev.holidayservice.cucumber.TestContext;
import com.github.tessdev.holidayservice.exception.ExternalServiceException;
import com.github.tessdev.holidayservice.model.Holiday;
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
        // no-op; LocalDate.now() used in production code
    }

    @Given("holidays are retrieved from the Nager public holidays API")
    public void holidays_are_retrieved_from_nager_api() {
        // handled via mocks (see later steps)
    }

    /* ---------- Given ---------- */

    @Given("the country code is {string}")
    public void the_country_code_is(String country) {
        context.countryCode = country;

        // Country not supported → empty result
        if ("AQ".equalsIgnoreCase(country)) {
            when(holidayService.getLastHolidays(anyString(), anyInt()))
                    .thenReturn(List.of());
            return;
        }

        // Default happy-path mock
        when(holidayService.getLastHolidays(anyString(), anyInt()))
                .thenReturn(List.of(
                        new Holiday("2024-10-03", "German Unity Day"),
                        new Holiday("2024-05-01", "Labour Day"),
                        new Holiday("2024-01-01", "New Year's Day")));
    }

    @Given("the Nager API is unavailable")
    public void the_nager_api_is_unavailable() {
        when(holidayService.getLastHolidays(anyString(), anyInt()))
                .thenThrow(new ExternalServiceException("Nager API down"));
    }

    @Given("an unexpected internal error occurs")
    public void unexpected_internal_error_occurs() {
        when(holidayService.getLastHolidays(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Unexpected error"));
    }

    /* ---------- When ---------- */
    @When("I request the last three holidays")
    public void i_request_last_three_holidays() throws Exception {
        var mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/api/holidays/last/{country}", context.countryCode)
                        .param("count", "3"))
                .andReturn();

        context.response = ResponseEntity
                .status(mvcResult.getResponse().getStatus())
                .body(mvcResult.getResponse().getContentAsString());
    }

    /* ---------- Then ---------- */
    @Then("the response status should be {int}")
    public void response_status_should_be(int status) {
        assertThat(context.response.getStatusCode().value()).isEqualTo(status);
    }

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
            String dateText = node.get("date").asText();
            assertThat(dateText).isNotBlank();
            assertThat(LocalDate.parse(dateText)).isBefore(today);
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
            String dateFirst = results.get(i).get("date").asText();
            assertThat(dateFirst).isNotBlank();

            String dateSecond = results.get(i + 1).get("date").asText();
            assertThat(dateSecond).isNotBlank();

            assertThat(LocalDate.parse(dateFirst)).isAfterOrEqualTo(LocalDate.parse(dateSecond));
        }
    }

    @Then("the response should be the same as for country {string}")
    public void response_same_as_country(String expectedCountry) throws Exception {
        String lowercaseResponse = context.response.getBody();

        var responseUpper = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/api/holidays/last/{country}", expectedCountry)
                        .param("count", "3"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(lowercaseResponse).isEqualTo(responseUpper);
    }

    @Then("an error message should be returned")
    public void error_message_returned() throws Exception {
        JsonNode root = objectMapper.readTree(context.response.getBody());
        assertThat(root.hasNonNull("message")).isTrue();
    }

    @Then("a service unavailable error should be returned")
    public void service_unavailable_error() throws Exception {
        JsonNode root = objectMapper.readTree(context.response.getBody());
        assertThat(root.get("code").asText()).isEqualTo("SERVICE_UNAVAILABLE");
    }

    @Then("the error code should be {string}")
    public void error_code_should_be(String code) throws Exception {
        JsonNode root = objectMapper.readTree(context.response.getBody());
        assertThat(root.get("code").asText()).isEqualTo(code);
    }
}
