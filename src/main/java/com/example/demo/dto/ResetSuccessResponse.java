package com.example.demo.dto;

public class ResetSuccessResponse {
    public boolean ok = true;
    public String message;

    public ResetSuccessResponse(String message) {
        this.message = message;
    }

}
