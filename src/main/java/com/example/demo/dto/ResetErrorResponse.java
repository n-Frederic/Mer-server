package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResetErrorResponse {
    @JsonProperty("error")
    public boolean isError = true;
    public String message;
    public String code;

    public ResetErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
    }
}
