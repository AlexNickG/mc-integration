package ru.skillbox.socialnetwork.integration.exception;

public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String serviceName, Throwable cause) {
        super("External service error: " + serviceName, cause);
    }

    public ExternalServiceException(String serviceName) {
        super("External service error: " + serviceName);
    }
}
