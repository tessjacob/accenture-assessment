package com.github.tessdev.holidayservice.exception;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(HolidayServiceException.class)
        public ResponseEntity<ErrorResponse> handleHolidayServiceException(
                        HolidayServiceException ex,
                        HttpServletRequest request) {

                ErrorResponse response = new ErrorResponse(
                                ex.getCode(),
                                ex.getMessage(),
                                Instant.now(),
                                request.getRequestURI(),
                                ex.getDetails());

                return ResponseEntity.status(ex.getStatus()).body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {

                ErrorResponse response = new ErrorResponse(
                                "INTERNAL_SERVER_ERROR",
                                "An unexpected error occurred",
                                Instant.now(),
                                request.getRequestURI(),
                                null);

                ex.printStackTrace(); // log stack trace
                return ResponseEntity.status(500).body(response);
        }
}