// PersonalTask.java
package com.example.demo.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personal_task")
public class PersonalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户ID，使用 Long 类型与 User 实体保持一致
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "personal_tasks", columnDefinition = "TEXT")
    private String personalTasksJson;

    @Transient
    private List<String> personalTasks;

    public PersonalTask() {}

    public PersonalTask(Long userId, List<String> personalTasks) {
        this.userId = userId;
        setPersonalTasks(personalTasks);
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPersonalTasksJson() { return personalTasksJson; }
    public void setPersonalTasksJson(String personalTasksJson) {
        this.personalTasksJson = personalTasksJson;
        this.personalTasks = jsonToList(personalTasksJson);
    }

    public List<String> getPersonalTasks() {
        if (personalTasks == null && personalTasksJson != null) {
            personalTasks = jsonToList(personalTasksJson);
        }
        return personalTasks != null ? personalTasks : new ArrayList<>();
    }

    public void setPersonalTasks(List<String> personalTasks) {
        this.personalTasks = personalTasks != null ? personalTasks : new ArrayList<>();
        this.personalTasksJson = listToJson(this.personalTasks);
    }

    // JSON 转换方法
    private String listToJson(List<String> list) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> jsonToList(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (personalTasks != null) {
            this.personalTasksJson = listToJson(personalTasks);
        }
    }

    @PostLoad
    public void postLoad() {
        if (personalTasksJson != null) {
            this.personalTasks = jsonToList(personalTasksJson);
        }
    }
}