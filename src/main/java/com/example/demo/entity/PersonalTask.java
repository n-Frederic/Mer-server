// PersonalTask.java
package com.example.demo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personal_task")
public class PersonalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户ID，直接存储，不建立关联
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    // 使用 JSON 格式将列表存储在单个字段中
    @Column(name = "personal_tasks", columnDefinition = "TEXT")
    private String personalTasksJson;

    // 临时字段，不持久化到数据库
    @Transient
    private List<String> personalTasks;

    // 构造方法
    public PersonalTask() {}

    public PersonalTask(Long userId, List<String> personalTasks) {
        this.userId = userId;
        setPersonalTasks(personalTasks); // 这会同时设置 personalTasks 和 personalTasksJson
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // 获取 personalTasksJson（数据库字段）
    public String getPersonalTasksJson() {
        return personalTasksJson;
    }

    public void setPersonalTasksJson(String personalTasksJson) {
        this.personalTasksJson = personalTasksJson;
        // 同时更新 transient 字段
        this.personalTasks = jsonToList(personalTasksJson);
    }

    // 获取 personalTasks（业务逻辑字段）
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

    // 在实体保存到数据库前的回调
    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (personalTasks != null) {
            this.personalTasksJson = listToJson(personalTasks);
        }
    }

    // 从数据库加载后的回调
    @PostLoad
    public void postLoad() {
        if (personalTasksJson != null) {
            this.personalTasks = jsonToList(personalTasksJson);
        }
    }
}