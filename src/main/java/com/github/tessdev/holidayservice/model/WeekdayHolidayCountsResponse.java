package com.github.tessdev.holidayservice.model;

import java.util.List;

public record WeekdayHolidayCountsResponse(
        int year,
        List<HolidayCountResult> results) {
}
