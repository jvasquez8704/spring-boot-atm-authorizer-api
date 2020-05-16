package com.bancatlan.atmauthorizer.exception;

//@ResponseStatus(HttpStatus.NOT_FOUND)
public class ModelNotFoundException extends RuntimeException {
    IError customError;

    public ModelNotFoundException(String message){
        super(message);
    }

    public ModelNotFoundException(String message, IError customError){
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
