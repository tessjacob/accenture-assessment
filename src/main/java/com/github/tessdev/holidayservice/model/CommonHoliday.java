package com.github.tessdev.holidayservice.model;

import java.time.LocalDate;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a common holiday with its date and local names.")
public record CommonHoliday(
                @Schema(description = "The date of the common holiday.", example = "2023-12-25") LocalDate date,
                @Schema(description = "The local names of the common holiday.") Map<String, String> localNames) {

        public CommonHoliday(LocalDate date, Map<String, String> localNames) {
                this.date = date;
                this.localNames = localNames;
        }
}
