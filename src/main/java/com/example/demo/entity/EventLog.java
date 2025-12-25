package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder

@AllArgsConstructor
@Entity
@Table(name = "event_log")
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    /**
     * 触发事件的用户ID，系统自动事件可以为 null
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 事件类型，如 TASK_CREATED、LOGIN_SUCCESS 等
     */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    /**
     * 事件创建时间
     * 如果用数据库的 DEFAULT CURRENT_TIMESTAMP，可以设置 insertable=false, updatable=false
     */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public EventLog(Long userId, String eventType, LocalDateTime createdAt, String targetType, Long targetId) {
        this.userId = userId;
        this.eventType = eventType;
        this.createdAt = createdAt;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    /**
     * 目标对象类型，如 task / log / report
     */
    @Column(name = "target_type", length = 50)
    private String targetType;

    /**
     * 目标对象ID，如 task_id / log_id / report_id
     */
    @Column(name = "target_id")
    private Long targetId;


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public EventLog() {
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

}

