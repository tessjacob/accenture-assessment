package com.github.tessdev.holidayservice.cucumber.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tessdev.holidayservice.cucumber.TestContext;
import com.github.tessdev.holidayservice.model.CommonHoliday;
import com.github.tessdev.holidayservice.model.CommonHolidaysResponse;
import com.github.tessdev.holidayservice.service.HolidayService;
import com.jayway.jsonpath.JsonPath;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CommonHolidaysDatesSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private TestContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private String country1;
    private String country2;

    /* ---------- Given ---------- */
    @Given("the first country code is {string}")
    public void the_first_country_code_is(String code) {
        this.country1 = code.toUpperCase();
    }

    @Given("the second country code is {string}")
    public void the_second_country_code_is(String code) {
        this.country2 = code.toUpperCase();
    }

    /* ---------- When ---------- */
    @When("I request the common public holidays")
    public void i_request_the_common_public_holidays() throws Exception {
        if (context.apiUnavailable) {
            context.response = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("{\"code\":\"SERVICE_UNAVAILABLE\"}");
            return;
        }

        // Simulate validation
        if (!country1.matches("[A-Z]{2}") || !country2.matches("[A-Z]{2}")) {
            context.response = ResponseEntity.badRequest()
                    .body("{\"message\":\"Invalid country code\"}");
            return;
        }

        if (context.year > LocalDate.now().getYear()) {
            context.response = ResponseEntity.badRequest()
                    .body("{\"message\":\"Year cannot be in the future\"}");
            return;
        } else {
            Map<String, String> localNames = new HashMap<>();
            localNames.put(country1, country1 + " Holiday");
            localNames.put(country2, country2 + " Holiday");

            // Happy path
            CommonHoliday holiday = new CommonHoliday(LocalDate.of(2024, 5, 1), localNames);
            CommonHolidaysResponse resp = new CommonHolidaysResponse(context.year, List.of(holiday));
            // context.response = ResponseEntity.ok(objectMapper.writeValueAsString(resp));
            when(holidayService.getCommonHolidays(context.year, country1, country2)).thenReturn(resp);
        }

        try {
            MvcResult result = mockMvc.perform(
                    get("/api/holidays/common")
                            .param("year", String.valueOf(context.year))
                            .param("country1", country1)
                            .param("country2", country2)
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn();

            String body = result.getResponse().getContentAsString();
            context.response = ResponseEntity
                    .status(result.getResponse().getStatus())
                    .body(body == null ? "" : body);

        } catch (Exception e) {
            context.response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Internal error\"}");
        }
    }

    /* ---------- Then ---------- */
    @Then("the response should be empty")
    public void the_response_should_be_empty() throws Exception {
        List<CommonHoliday> counts = JsonPath.read(context.response.getBody(), "$.results[*].count");
        assertTrue(counts.isEmpty());
    }

    @Then("the response should contain common holiday dates")
    public void the_response_should_contain_common_holiday_dates() throws Exception {
        List<Map<String, String>> localNamesList = JsonPath.read(context.response.getBody(), "$.results[*].localNames");
        assertFalse(localNamesList.isEmpty());
    }

    @Then("the response should not contain duplicate dates")
    public void the_response_should_not_contain_duplicate_dates() throws Exception {
        CommonHolidaysResponse resp = objectMapper.readValue(context.response.getBody(), CommonHolidaysResponse.class);
        Set<LocalDate> seen = new HashSet<>();
        for (CommonHoliday h : resp.results()) {
            assertTrue(seen.add(h.date()));
        }
    }

    @Then("each holiday date should contain:")
    public void each_holiday_date_should_contain(DataTable dataTable)
            throws JsonMappingException, JsonProcessingException {
        assertNotNull(context.response, "Response is null");
        CommonHolidaysResponse resp = objectMapper.readValue(context.response.getBody(), CommonHolidaysResponse.class);

        dataTable.asMaps().forEach(row -> {
            String expectedCountry = row.get("country");
            String expectedName = row.get("localName");
            resp.results().forEach(h -> System.out.println("Date: " + h.date() + ", LocalNames: " + h.localNames()));
            boolean found = resp.results().stream()
                    .anyMatch(h -> h.localNames().get(expectedCountry).equals(expectedName));
            assertTrue(found, "Expected holiday not found: " + expectedCountry + " -> " + expectedName);
        });
    }

    @Then("each date should appear only once")
    public void each_date_should_appear_only_once() throws JsonMappingException, JsonProcessingException {
        assertNotNull(context.response, "Response is null");
        CommonHolidaysResponse resp = objectMapper.readValue(context.response.getBody(), CommonHolidaysResponse.class);
        long uniqueDates = resp.results().stream().map(CommonHoliday::date).distinct().count();
        assertThat(uniqueDates).isEqualTo(resp.results().size());
    }

    @Then("each date should include local names from both countries")
    public void each_date_should_include_local_names_from_both_countries() throws Exception {
        CommonHolidaysResponse resp = objectMapper.readValue(context.response.getBody(), CommonHolidaysResponse.class);

        for (CommonHoliday h : resp.results()) {
            assertTrue(h.localNames().containsKey(country1));
            assertTrue(h.localNames().containsKey(country2));
        }
    }

    @Then("all public holiday dates for the year should be returned")
    public void all_public_holiday_dates_for_the_year_should_be_returned()
            throws JsonMappingException, JsonProcessingException {
        assertNotNull(context.response, "Response is null");
        CommonHolidaysResponse resp = objectMapper.readValue(context.response.getBody(), CommonHolidaysResponse.class);
        assertNotNull(resp, "Response body is null");
        assertFalse(resp.results().isEmpty(), "Expected all holiday dates, but list was empty");
    }

    @Then("each result should contain:")
    public void each_result_should_contain(DataTable dataTable) throws Exception {

        // Deserialize JSON response body
        CommonHolidaysResponse resp = objectMapper.readValue(context.response.getBody(), CommonHolidaysResponse.class);

        List<CommonHoliday> results = resp.results();
        assertFalse(results.isEmpty(), "Results should not be empty");

        // Expected fields from DataTable
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        for (CommonHoliday holiday : results) {
            for (Map<String, String> row : rows) {
                String field = row.get("field");
                switch (field) {
                    case "date" ->
                        assertNotNull(holiday.date(), "date should be present");
                    case "localNames" ->
                        assertNotNull(holiday.localNames(), "localNames should be present");
                    default ->
                        throw new IllegalArgumentException("Unknown field: " + field);
                }
            }
        }
    }
}
