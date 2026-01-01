package com.github.tessdev.holidayservice.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the most recently celebrated holidays")
public record LastHolidaysResponse(

        @Schema(description = "ISO 3166-1 alpha-2 country code", example = "NL", pattern = "^[A-Z]{2}$") String country,

        @Schema(description = "List of recent public holidays (max 3)") List<Holiday> results,

        @Schema(description = "Number of holidays returned", example = "3", minimum = "0", maximum = "3") int count) {
}
