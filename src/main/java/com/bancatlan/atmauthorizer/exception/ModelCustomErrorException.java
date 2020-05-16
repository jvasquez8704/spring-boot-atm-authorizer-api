package com.bancatlan.atmauthorizer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//@ResponseStatus(HttpStatus.METHOD_FAILURE)
public class ModelCustomErrorException extends RuntimeException {
    IError customError;

    public ModelCustomErrorException(String message){
        super(message);
    }
    public ModelCustomErrorException(String message, IError customError){
        super(message);
        this.customError = customError;
    }

    public IError getCustomError() {
        return customError;
    }

    public void setCustomError(IError customError) {
        this.customError = customError;
    }
}
