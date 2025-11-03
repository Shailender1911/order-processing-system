package com.peerislands.orderprocessingsystem.web.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> validationErrors
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public static ApiError withValidationErrors(
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
    ) {
        Map<String, String> errors = (validationErrors == null || validationErrors.isEmpty()) ? null : Map.copyOf(validationErrors);
        return new ApiError(Instant.now(), status, error, message, path, errors);
    }
}

