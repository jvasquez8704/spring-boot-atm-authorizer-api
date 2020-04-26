package com.bancatlan.atmauthorizer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//@ResponseStatus(HttpStatus.NOT_FOUND)
public class ModelNotFoundException extends RuntimeException {
    AuthorizerError customError;

    public ModelNotFoundException(String message){
        super(message);
    }

    public ModelNotFoundException(String message, AuthorizerError customError){
        super(message);
        this.customError = customError;
    }

    public AuthorizerError getCustomError() {
        return customError;
    }

    public void setCustomError(AuthorizerError customError) {
        this.customError = customError;
    }
}
