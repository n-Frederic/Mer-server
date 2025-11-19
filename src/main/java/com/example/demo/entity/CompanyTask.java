package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "company_task")
public class CompanyTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long task_id;

    private String title;


    public CompanyTask() {}

    // Getter & Setter
    public Long getTaskId() { return task_id; }
    public void setTaskId(Long taskId) { this.task_id = taskId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

}