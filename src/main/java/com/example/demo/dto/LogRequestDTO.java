package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;

import java.time.LocalDate;
import java.util.List;

public class LogRequestDTO {
    @JsonProperty("log_date")
    private LocalDate logDate;

    @JsonProperty("todaySummary")
    private String summary;

    @JsonProperty("tomorrowPlan")
    private String tomorrowPlan;

    @JsonProperty("helpNeeded")
    private String helpNeeded;


    @JsonProperty("taskId")
    private List<Long> taskId;

    public LocalDate getLogDate() { return logDate; }

    public String getSummary() { return summary; }

    public String getTomorrowPlan() {
        return tomorrowPlan;
    }

    public String getHelpNeeded() {
        return helpNeeded;
    }
    public List<Long> getTaskId() {
        return taskId;
    }

}
