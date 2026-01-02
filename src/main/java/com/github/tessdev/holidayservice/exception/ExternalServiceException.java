package com.github.tessdev.holidayservice.exception;

public class ExternalServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String serviceUrl;

    public ExternalServiceException(String message) {
        super(message);
        this.serviceUrl = null;
    }

    public ExternalServiceException(String message, String serviceUrl) {
        super(message);
        this.serviceUrl = serviceUrl;
    }

    public ExternalServiceException(String message, String serviceUrl, Throwable cause) {
        super(message, cause);
        this.serviceUrl = serviceUrl;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }
}
