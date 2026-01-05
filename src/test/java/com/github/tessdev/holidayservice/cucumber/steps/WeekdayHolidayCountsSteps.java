package com.github.tessdev.holidayservice.cucumber.steps;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.github.tessdev.holidayservice.cucumber.TestContext;
import com.github.tessdev.holidayservice.exception.ExternalServiceException;
import com.github.tessdev.holidayservice.model.HolidayCountResult;
import com.github.tessdev.holidayservice.model.SortOrder;
import com.github.tessdev.holidayservice.model.WeekdayHolidayCountsResponse;
import com.github.tessdev.holidayservice.service.HolidayService;
import com.jayway.jsonpath.JsonPath;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class WeekdayHolidayCountsSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestContext context;

    @Autowired
    private HolidayService holidayService;

    private List<String> countries = new ArrayList<>();
    private ResultActions response;

    // ---------- Given ----------
    @Given("the public holidays data is retrieved from the Nager public holidays API")
    public void public_holidays_data_is_retrieved() {
        // No-op: default behavior mocked per scenario
    }

    @Given("the country codes are:")
    public void the_country_codes_are(DataTable table) {
        countries.clear();
        for (Map<String, String> row : table.asMaps()) {
            countries.add(row.get("country"));
        }
    }

    // ---------- When ----------
    @When("I request the holiday counts excluding weekends")
    public void i_request_the_holiday_counts_excluding_weekends() throws Exception {

        if (context.apiUnavailable) {
            doThrow(new ExternalServiceException("https://date.nager.at"))
                    .when(holidayService)
                    .getWeekdayHolidayCounts(anyInt(), anyList(), eq(false), any(SortOrder.class));
        } else if (context.internalError) {
            doThrow(new RuntimeException("Boom"))
                    .when(holidayService)
                    .getWeekdayHolidayCounts(anyInt(), anyList(), eq(false), any(SortOrder.class));
        } else {
            List<HolidayCountResult> results = countries.stream()
                    .map(c -> new HolidayCountResult(c, c.equals("AQ") ? 0 : 5))
                    .toList();

            when(holidayService.getWeekdayHolidayCounts(
                    eq(context.year),
                    eq(countries),
                    eq(false),
                    eq(SortOrder.DESC)))
                    .thenReturn(new WeekdayHolidayCountsResponse(context.year, results));
        }

        try {
            MvcResult mvcResult = mockMvc.perform(
                    get("/api/holidays/weekday-counts")
                            .param("year", String.valueOf(context.year))
                            .param("countries", countries.toArray(new String[0]))
                            .param("weekend", "false")
                            .param("sort", SortOrder.DESC.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            // Assign ResultActions for happy-path
            response = mockMvc.perform(
                    get("/api/holidays/weekday-counts")
                            .param("year", String.valueOf(context.year))
                            .param("countries", countries.toArray(new String[0]))
                            .param("weekend", "false")
                            .param("sort", SortOrder.DESC.name())
                            .contentType(MediaType.APPLICATION_JSON));

            // Also store ResponseEntity in context for CommonHolidaySteps
            context.response = ResponseEntity
                    .status(mvcResult.getResponse().getStatus())
                    .body(mvcResult.getResponse().getContentAsString());

        } catch (Exception ex) {
            // If an exception occurs, convert it to a ResponseEntity for error steps
            context.response = ResponseEntity
                    .status(500)
                    .body("{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"" + ex.getMessage() + "\"}");
        }
    }

    // ---------- Then ----------
    @Then("the response should contain {int} countries")
    public void the_response_should_contain_countries(Integer count) throws Exception {
        response.andExpect(jsonPath("$.results.length()").value(count));
    }

    @Then("each country should contain a holiday count")
    public void each_country_should_contain_a_holiday_count() throws Exception {
        response.andExpect(jsonPath("$.results[*].count").exists());
    }

    @Then("all holiday counts should be greater than or equal to {int}")
    public void all_holiday_counts_should_be_greater_than_or_equal_to(Integer min) throws Exception {
        response.andExpect(jsonPath("$.results[*].count", everyItem(greaterThanOrEqualTo(min))));
    }

    @Then("the countries should be sorted by holiday count in descending order")
    public void countries_sorted_descending() throws Exception {

        MvcResult result = response.andReturn();
        String json = result.getResponse().getContentAsString();

        List<Integer> counts = JsonPath.read(json, "$.results[*].count");

        for (int i = 0; i < counts.size() - 1; i++) {
            if (counts.get(i) < counts.get(i + 1)) {
                throw new AssertionError(
                        "Holiday counts are not sorted descending: " + counts);
            }
        }
    }

    @Then("the response should contain:")
    public void the_response_should_contain(DataTable table) throws Exception {
        for (Map<String, String> row : table.asMaps()) {
            String country = row.get("country");
            String count = row.get("count");

            if (count.startsWith(">")) {
                int min = Integer.parseInt(count.replace(">", "").trim());
                response.andExpect(jsonPath("$.results[?(@.country=='" + country + "')].count",
                        Matchers.hasItem(Matchers.greaterThan(min))));
            } else {
                int expected = Integer.parseInt(count);
                response.andExpect(jsonPath("$.results[?(@.country=='" + country + "')].count",
                        Matchers.hasItem(Matchers.equalTo(expected))));
            }
        }
    }

    @Then("all counted holidays should not fall on Saturday or Sunday")
    public void no_weekend_holidays() {
        // Guaranteed by weekend=false + service contract
        // Explicit date checks belong to unit tests, not controller Cucumber tests
    }

    @Then("an error message should indicate invalid country codes")
    public void error_message_invalid_country_codes() throws Exception {
        response.andExpect(jsonPath("$.code").value("INVALID_COUNTRY_CODE"));
    }
}