// PersonalTask.java
package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "personal_task")
public class PersonalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联到 User 实体
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(name = "personal_task_item", joinColumns = @JoinColumn(name = "personal_task_id"))
    @Column(name = "personal_task")
    private List<String> personalTasks;

    // 构造方法
    public PersonalTask() {}

    public PersonalTask(User user, List<String> personalTasks) {
        this.user = user;
        this.personalTasks = personalTasks;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<String> getPersonalTasks() {
        return personalTasks;
    }

    public void setPersonalTasks(List<String> personalTasks) {
        this.personalTasks = personalTasks;
    }
}