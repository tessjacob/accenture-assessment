package com.github.tessdev.holidayservice.model;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Public holiday information")
public record Holiday(
                @Schema(example = "2025-05-29") LocalDate date,

                @Schema(example = "Hemelvaartsdag") String name) {

        public Holiday(String string, String string2) {
                this(LocalDate.parse(string), string2);
        }
}