package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notif_id")
    private Long notifId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "type")
    private String type;

    @Column(name = "relevent_id")
    private Long releventId;

    @Column(name = "title")
    private String title;

    @Column(name="body")
    private String body;

    @Column(name="is_read")
    private Boolean isRead;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Long getReleventId() {
        return releventId;
    }

    public void setReleventId(Long releventId) {
        this.releventId = releventId;
    }

    public Notification(Long userId, String type, Long releventId,String title, String body, Boolean isRead) {

        this.userId = userId;
        this.type = type;
        this.title = title;
        this.releventId = releventId;
        this.body = body;
        this.isRead = isRead;
        this.createdAt=LocalDateTime.now();
    }

    public Long getNotifId() {
        return notifId;
    }

    public void setNotifId(Long notifId) {
        this.notifId = notifId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
