package com.github.tessdev.holidayservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(ExternalServiceException.class)
        @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
        public ResponseEntity<ErrorResponse> handleExternalServiceException(
                        ExternalServiceException ex, WebRequest request) {

                logger.error("External service error from {}: {} (Status: {})",
                                ex.getServiceUrl(), ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                "SERVICE_UNAVAILABLE",
                                "SERVICE_UNAVAILABLE",
                                "External service is temporarily unavailable. Please try again later.",
                                request.getDescription(false).replace("uri=", ""));

                return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
        }

        @ExceptionHandler(InvalidCountryCodeException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ErrorResponse> handleInvalidCountry(InvalidCountryCodeException ex,
                        HttpServletRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                "INVALID_COUNTRY_CODE",
                                "INVALID_COUNTRY_CODE",
                                "Country code must be a valid ISO 3166-1 alpha-2 code.",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(CountryNotSupportedException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ResponseEntity<ErrorResponse> handleCountryNotSupported(CountryNotSupportedException ex,
                        HttpServletRequest request) {
                ErrorResponse errorResponse = new ErrorResponse(
                                "COUNTRY_NOT_SUPPORTED",
                                "COUNTRY_NOT_SUPPORTED",
                                "No holiday data found for the specified country.",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(InvalidYearException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ErrorResponse> handleInvalidYearException(InvalidYearException ex,
                        HttpServletRequest request) {
                logger.error("Unhandled exception: {}", ex.getMessage(), ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                "BAD_REQUEST",
                                "BAD_REQUEST",
                                "An unexpected error occurred. Please try again later.",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(InvalidRequestException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException ex,
                        HttpServletRequest request) {
                logger.error("Unhandled exception: {}", ex.getMessage(), ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                "BAD_REQUEST",
                                "BAD_REQUEST",
                                ex.getMessage(),
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex,
                        HttpServletRequest request) {
                logger.error("Unhandled exception: {}", ex.getMessage(), ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                "INTERNAL_SERVER_ERROR",
                                "GENERIC_ERROR",
                                "An unexpected error occurred. Please try again later.",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}