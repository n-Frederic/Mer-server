package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskProgressUpdateDTO {

    @JsonProperty("taskId")
    private String taskId;

    @JsonProperty("progressPct")
    private Long progressPct;

    public String getTaskId() {
        return taskId;
    }

    public Long getProgressPct() {
        return progressPct;
    }
}
