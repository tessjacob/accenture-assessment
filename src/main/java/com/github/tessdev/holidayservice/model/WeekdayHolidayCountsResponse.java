package com.github.tessdev.holidayservice.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the count of weekday holidays per country for a specific year.")
public record WeekdayHolidayCountsResponse(
                @Schema(description = "Year for which the counts are provided", example = "2024") int year,
                @Schema(description = "List of holiday count results per country") List<HolidayCountResult> results) {
}
