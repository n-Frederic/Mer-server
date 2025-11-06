package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "log_date")
    private LocalDate date;
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User author;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "view_type")
    private String viewType;

    @Column(name = "mood")
    private String mood;


    private Instant createdAt;
    private Instant updatedAt;

    public Log() {}

    public Log(String title, LocalDate date, String summary, String content, String mood, String viewType, Long taskId, User author) {
        this.title = title;
        this.date = date;
        this.summary = summary;
        this.content = content;
        this.author = author;
        this.mood = mood;
        this.viewType = viewType;
        this.taskId = taskId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();

    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
