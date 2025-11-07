package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LogTaskId implements Serializable {
    @Column(name = "log_id")
    private Long logId;
    @Column(name = "task_id")
    private Long taskId;

    public LogTaskId() {}

    public LogTaskId(Long logId, Long taskId) {
        this.logId = logId;
        this.taskId = taskId;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogTaskId)) return false;
        LogTaskId that = (LogTaskId) o;
        return Objects.equals(logId, that.logId) &&
                Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId, taskId);
    }
}
