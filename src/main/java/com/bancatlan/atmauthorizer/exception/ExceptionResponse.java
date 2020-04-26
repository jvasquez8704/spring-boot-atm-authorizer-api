package com.bancatlan.atmauthorizer.exception;

import java.time.LocalDateTime;

public class ExceptionResponse {

    private LocalDateTime timestamp;
    private String message;
    private String details;
    private AuthorizerError customError;

    public ExceptionResponse(LocalDateTime timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public ExceptionResponse(LocalDateTime timestamp, String message, String details, AuthorizerError customError) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.customError = customError;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public AuthorizerError getCustomError() {
        return customError;
    }

    public void setCustomError(AuthorizerError customError) {
        this.customError = customError;
    }
}
