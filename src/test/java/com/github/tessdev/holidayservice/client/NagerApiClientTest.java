package com.github.tessdev.holidayservice.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tessdev.holidayservice.exception.ExternalServiceException;
import com.github.tessdev.holidayservice.model.Holiday;

class NagerApiClientTest {

        @Test
        @DisplayName("getHolidays success returns list of holidays.")
        void getHolidaysSuccesseturnsList() throws Exception {
                HttpClient mockClient = mock(HttpClient.class);
                HttpResponse<String> mockResponse = mock(HttpResponse.class);

                String json = "[{\r\n" + //
                                "        \"date\": \"2025-12-25\",\r\n" + //
                                "        \"localName\": \"Eerste Kerstdag\",\r\n" + //
                                "        \"name\": \"Christmas Day\",\r\n" + //
                                "        \"countryCode\": \"NL\",\r\n" + //
                                "        \"fixed\": false,\r\n" + //
                                "        \"global\": true,\r\n" + //
                                "        \"counties\": null,\r\n" + //
                                "        \"launchYear\": null,\r\n" + //
                                "        \"types\": [\r\n" + //
                                "            \"Public\"\r\n" + //
                                "        ]\r\n" + //
                                "    }]";

                when(mockResponse.statusCode()).thenReturn(200);
                when(mockResponse.body()).thenReturn(json);
                when(mockClient.send(org.mockito.ArgumentMatchers.any(HttpRequest.class),
                                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                                .thenReturn(mockResponse);

                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

                NagerApiClient client = new NagerApiClient(mockClient, mapper);

                List<Holiday> holidays = client.getHolidays(2025, "NL");

                assertEquals(1, holidays.size());
                assertEquals("Eerste Kerstdag", holidays.get(0).localName());
                assertEquals(java.time.LocalDate.of(2025, 12, 25), holidays.get(0).date());
        }

        @Test
        @DisplayName("getHolidays non-200 response throws ExternalServiceException.")
        void getHolidaysNon200ThrowsExternalServiceException() throws Exception {
                HttpClient mockClient = mock(HttpClient.class);
                HttpResponse<String> mockResponse = mock(HttpResponse.class);

                when(mockResponse.statusCode()).thenReturn(500);
                when(mockClient.send(org.mockito.ArgumentMatchers.any(HttpRequest.class),
                                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                                .thenReturn(mockResponse);

                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

                NagerApiClient client = new NagerApiClient(mockClient, mapper);

                ExternalServiceException ex = assertThrows(ExternalServiceException.class,
                                () -> client.getHolidays(2025, "US"));

                // message should contain status code
                assertEquals(true, ex.getMessage().contains("External service temporarily unavailable"));
        }

        @Test
        @DisplayName("getHolidays IOException throws ExternalServiceException.")
        void getHolidaysIOExceptionThrowsExternalServiceException() throws Exception {
                HttpClient mockClient = mock(HttpClient.class);

                when(mockClient.send(org.mockito.ArgumentMatchers.any(HttpRequest.class),
                                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                                .thenThrow(new IOException("network error"));

                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

                NagerApiClient client = new NagerApiClient(mockClient, mapper);

                assertThrows(ExternalServiceException.class, () -> client.getHolidays(2025, "US"));
        }
}
