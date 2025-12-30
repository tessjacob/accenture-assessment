package com.github.tessdev.holidayservice.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tessdev.holidayservice.exception.ExternalServiceException;
import com.github.tessdev.holidayservice.model.Holiday;

@Component
public class NagerApiClient {

    private static final String BASE_URL = "https://date.nager.at/Api/v3";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NagerApiClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public List<Holiday> getHolidays(int year, String countryCode) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/PublicHolidays/" + year + "/" + countryCode))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ExternalServiceException(
                        "Nager.Date API returned status code: " + response.statusCode(),
                        BASE_URL);
            }

            return List.of(objectMapper.readValue(response.body(), Holiday[].class));
        } catch (IOException | InterruptedException e) {
            throw new ExternalServiceException(
                    "Failed to fetch holidays from Nager.Date API",
                    "https://date.nager.at/Api/v3",
                    e);
        }
    }
}
