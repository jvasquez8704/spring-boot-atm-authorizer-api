package com.bancatlan.atmauthorizer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//@ResponseStatus(HttpStatus.METHOD_FAILURE)
public class ModelCustomErrorException extends RuntimeException {
    AuthorizerError customError;

    public ModelCustomErrorException(String message){
        super(message);
    }
    public ModelCustomErrorException(String message, AuthorizerError customError){
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
