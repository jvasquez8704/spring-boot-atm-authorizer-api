package com.bancatlan.atmauthorizer.api.http;

public class CustomResponse<T> {
    private T data;
    private T status;

    public CustomResponse(T data, T status){
        this.data = data;
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getStatus() {
        return status;
    }

    public void setStatus(T status) {
        this.status = status;
    }
}
