package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "personal_tasks")
public class PersonalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "personal_task_items", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "task")
    private List<String> tasks;

    // 构造方法
    public PersonalTask() {}

    public PersonalTask(List<String> tasks) {
        this.tasks = tasks;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }
}
