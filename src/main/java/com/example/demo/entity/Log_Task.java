package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="log_task_map")
public class Log_Task {
    @EmbeddedId
    private LogTaskId id;

    @ManyToOne
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    public Log_Task() {}

    public Log_Task(Long logId, Long taskId) {
        this.id = new LogTaskId(logId, taskId);
    }

}
