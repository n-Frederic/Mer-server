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

    @Column(name = "log_date")
    private LocalDate date;

    @Column(name = "today_summary")
    private String summary;

    @Column(name = "tomorrow_plan")
    private String tomorrowPlan;

    @Column(name = "help_needed")
    private String helpNeeded;

    @Column(name = "status")
    private String status;



    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User author;

    private Instant createdAt;
    private Instant updatedAt;

    public Log() {}

    public Log(LocalDate date, String summary, String tomorrowPlan, String helpNeeded, User author) {
        this.date = date;
        this.summary = summary;
        this.tomorrowPlan = tomorrowPlan;
        this.helpNeeded = helpNeeded;
        this.status = "待审批";
        this.author = author;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }


    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }


    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getTomorrowPlan() {
        return tomorrowPlan;
    }

    public void setTomorrowPlan(String tomorrowPlan) {
        this.tomorrowPlan = tomorrowPlan;
    }

    public String getHelpNeeded() {
        return helpNeeded;
    }

    public void setHelpNeeded(String helpNeeded) {
        this.helpNeeded = helpNeeded;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
