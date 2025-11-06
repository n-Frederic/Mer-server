package com.example.demo.dto;

import java.time.LocalDate;

public class LogRequestDTO {
    private String title;
    private LocalDate LogDate;
    private String summary;
    private String content;
    private String viewType;
    private String mood;
    private Long taskId;

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    // Getter & Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getLogDate() { return LogDate; }
    public void setLogDate(LocalDate logDate) { this.LogDate = logDate; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
