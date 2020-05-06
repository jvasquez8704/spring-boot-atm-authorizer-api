package com.bancatlan.atmauthorizer.api.http;

public class CustomStatus {
    private int code;
    private String type;
    private String message;
    private String detail;

    public CustomStatus(int code, String type, String message, String detail){
        this.code = code;
        this.type = type;
        this.message = message;
        this.detail = detail;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
