package com.github.tessdev.holidayservice.model;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Public holiday information")
public record Holiday(
                @Schema(description = "Date of the holiday", example = "2025-05-29") LocalDate date,
                @Schema(description = "Name of the holiday", example = "Hemelvaartsdag") String localName) {

        public Holiday(String string, String string2) {
                this(LocalDate.parse(string), string2);
        }

        public String getDate() {
                return date.toString();
        }

        public String getLocalName() {
                return localName;
        }
}