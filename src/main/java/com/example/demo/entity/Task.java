package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", referencedColumnName = "user_id")
    private User creator;

    @Column(name = "title")
    private String title;

    private String description;

    private String priority;
    private String status;
    private Instant startAt;
    private Instant dueAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Task() {}

    public Task(Instant updatedAt, Instant createdAt, Instant dueAt, Instant startAt, String status, String priority, String description, String title, User creator) {
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.dueAt = dueAt;
        this.startAt = startAt;
        this.status = status;
        this.priority = priority;
        this.description = description;
        this.title = title;
        this.creator = creator;

    }

    // Getter & Setter
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public User getCreator() {
        return creator;
    }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getStartAt() { return startAt; }
    public Instant getDueAt() { return dueAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
