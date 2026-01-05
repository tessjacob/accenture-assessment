package com.github.tessdev.holidayservice.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing common holidays for a specific year.")
public record CommonHolidaysResponse(
                @Schema(description = "The year for which the common holidays are returned.", example = "2023") int year,
                @Schema(description = "The list of common holidays for the specified year.") List<CommonHoliday> results) {

        public CommonHolidaysResponse(int year, List<CommonHoliday> results) {
                this.year = year;
                this.results = results;
        }
}
