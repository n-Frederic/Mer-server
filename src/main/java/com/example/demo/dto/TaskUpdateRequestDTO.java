package com.example.demo.dto;

import java.time.Instant;
import java.util.List;

public class TaskUpdateRequestDTO {

    private String title;
    private String description;
    private String priority;
    private String status;
    private Instant startAt;
    private Instant dueAt;
    private String parentTask;
    private List<String> tags;

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public Instant getStartAt() { return startAt; }
    public Instant getDueAt() { return dueAt; }
    public String getParentTask() { return parentTask; }
    public List<String> getTags() { return tags; }
}
