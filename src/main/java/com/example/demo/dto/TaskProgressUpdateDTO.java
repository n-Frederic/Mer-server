package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskProgressUpdateDTO {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("progressPct")
    private Integer progressPct;

    public String getUserId() {
        return userId;
    }

    public Integer getProgressPct() {
        return progressPct;
    }
}
