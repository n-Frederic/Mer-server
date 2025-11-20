package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "task")
public class Task {
    // Getter & Setter
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "creator_id", referencedColumnName = "user_id")
    private User creator;

    @Setter
    @Getter
    @Column(name = "title")
    private String title;

    @Setter
    @Getter
    @Column(name="description")
    private String description;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tags> tags = new HashSet<>();

    @Setter
    @Getter
    private String priority;
    @Setter
    @Getter
    private String status;
    @Setter
    @Getter
    private Long progress_pct;
    @Setter
    @Getter
    private Instant startAt;
    @Setter
    @Getter
    private Instant dueAt;
    @Setter
    @Getter
    private Instant createdAt;
    @Setter
    @Getter
    private Instant updatedAt;


    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "parent_task", referencedColumnName = "task_id")
    @JsonIgnore
    private Task parent_task;

    public Task() {}

    public Task(Instant updatedAt, Instant createdAt, Instant dueAt, Instant startAt, String status,Long progress_pct, String priority, String description, String title, User creator) {
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.dueAt = dueAt;
        this.startAt = startAt;
        this.status = status;
        this.progress_pct = progress_pct;
        this.priority = priority;
        this.description = description;
        this.title = title;
        this.creator = creator;

    }


}
