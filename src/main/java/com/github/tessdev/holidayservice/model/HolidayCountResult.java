package com.github.tessdev.holidayservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result object representing the count of holidays for a specific country.")
public record HolidayCountResult(
        @Schema(description = "ISO 3166-1 alpha-2 country code", example = "NL") String country,
        @Schema(description = "Count of holidays for the country", example = "10") int count) {
}