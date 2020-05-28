package com.bancatlan.atmauthorizer.exception;


import com.bancatlan.atmauthorizer.api.http.AtmBody;

public class ModelAtmErrorException extends RuntimeException {
    IError customError;
    Object data;

    public ModelAtmErrorException(String message){
        super(message);
    }
    public ModelAtmErrorException(String message, IError customError){
        super(message);
        this.customError = customError;
    }
    public ModelAtmErrorException(String message, IError customError, Object data){
        super(message);
        this.customError = customError;
        this.data = data;
    }


    public IError getCustomError() {
        return customError;
    }

    public void setCustomError(IError customError) {
        this.customError = customError;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
