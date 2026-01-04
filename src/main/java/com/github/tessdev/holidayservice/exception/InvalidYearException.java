package com.github.tessdev.holidayservice.exception;

public class InvalidYearException extends RuntimeException {
    public InvalidYearException(String messageString) {
        super(messageString);
    }

}
