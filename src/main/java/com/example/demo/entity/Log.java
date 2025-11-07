package com.example.demo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "log_date")
    private LocalDate date;

    @Column(name = "todaySummary")
    private String summary;


    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User author;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> taskId;

    private Instant createdAt;
    private Instant updatedAt;

    public Log() {}

    public Log(LocalDate date, String summary, List<String> taskId, User author) {
        this.date = date;
        this.summary = summary;
        this.author = author;
        this.taskId = taskId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<String> getTaskId() {
        return taskId;
    }


    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }


    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
