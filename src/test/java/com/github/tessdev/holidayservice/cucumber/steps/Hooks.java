package com.github.tessdev.holidayservice.cucumber.steps;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.tessdev.holidayservice.cucumber.TestContext;

import io.cucumber.java.Before;

public class Hooks {

    @Autowired
    private TestContext context;

    @Before
    public void resetContext() {
        context.countryCode = null;
        context.response = null;
        context.apiUnavailable = false;
        context.internalError = false;
    }
}
