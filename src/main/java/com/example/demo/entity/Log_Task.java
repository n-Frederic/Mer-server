package com.example.demo.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="log_task_map")
public class Log_Task {
    @EmbeddedId
    private LogTaskId id;


    public Log_Task() {}

    public Log_Task(Long logId, Long taskId) {
        this.id = new LogTaskId(logId, taskId);
    }

    public LogTaskId getId() {
        return id;
    }

    public void setId(LogTaskId id) {
        this.id = id;
    }
}
