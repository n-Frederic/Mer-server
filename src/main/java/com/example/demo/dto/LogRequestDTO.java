package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

public class LogRequestDTO {
    private LocalDate log_date;
    private String todaySummary;
    private String tomorrowPlan;
    private String helpNeeded;
    private String content;

    private List<String> taskId;



    public List<String> getTaskId() {
        return taskId;
    }

    public LocalDate getLog_date() { return log_date; }
    public void setLog_date(LocalDate log_date) { this.log_date = log_date; }

    public String getTodaySummary() { return todaySummary; }
    public void setTodaySummary(String todaySummary) { this.todaySummary = todaySummary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTomorrowPlan() {
        return tomorrowPlan;
    }

    public String getHelpNeeded() {
        return helpNeeded;
    }
}
