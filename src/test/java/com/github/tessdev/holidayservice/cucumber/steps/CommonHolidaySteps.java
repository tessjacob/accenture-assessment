package com.github.tessdev.holidayservice.cucumber.steps;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tessdev.holidayservice.cucumber.TestContext;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class CommonHolidaySteps {
    @Autowired
    private TestContext context;

    @Autowired
    private ObjectMapper objectMapper;

    /* ---------- Given ---------- */
    @Given("the Nager API is unavailable")
    public void set_api_unavailable() {
        context.apiUnavailable = true;
    }

    @Given("an unexpected internal error occurs")
    public void set_internal_error() {
        context.internalError = true;
    }

    @Given("the year is {int}")
    public void the_year_is(Integer year) {
        context.year = year;
    }

    /* ---------- Then ---------- */
    @Then("the response status should be {int}")
    public void response_status_should_be(int status) {
        assertThat(context.response.getStatusCode().value()).isEqualTo(status);
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

    @Then("an error message should be returned")
    public void an_error_message_should_be_returned() throws Exception {
        JsonNode root = objectMapper.readTree(context.response.getBody());
        assertThat(root.get("message").asText()).isNotEmpty();
    }
}
