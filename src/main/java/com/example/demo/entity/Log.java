package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "log")
public class Log {

    // Getter / Setter
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Getter
    @Setter
    @Column(name = "log_date")
    private LocalDate date;

    @Getter
    @Setter
    @Column(name = "today_summary")
    private String summary;

    @Getter
    @Setter
    @Column(name = "tomorrow_plan")
    private String tomorrowPlan;

    @Getter
    @Setter
    @Column(name = "help_needed")
    private String helpNeeded;

    @Getter
    @Setter
    @Column(name = "status")
    private String status;



    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User author;

    @Getter
    @Setter
    private LocalDateTime createdAt;
    @Getter
    @Setter
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "id.logId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Log_Task> logTasks = new HashSet<>();

    @Transient
    public Set<Task> getTasks() {
        Set<Task> tasks = new HashSet<>();
        for (Log_Task lt : logTasks) {
            tasks.add(lt.getTask());
        }
        return tasks;
    }

    public Log() {}

    public Log(LocalDate date, String summary, String tomorrowPlan, String helpNeeded, User author) {
        this.date = date;
        this.summary = summary;
        this.tomorrowPlan = tomorrowPlan;
        this.helpNeeded = helpNeeded;
        this.status = "待审批";
        this.author = author;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}
