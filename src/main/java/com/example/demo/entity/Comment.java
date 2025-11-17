package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;



    @Column(name = "owner_id", nullable = false)
    private Long ownerId;       // 关联任务或日志


    @JoinColumn(name = "log_id", referencedColumnName = "log_id")
    private Long logId;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Comment() {}

    public Comment(Long logId, Long ownerId, String content) {
        this.logId = logId;
        this.ownerId = ownerId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // Getter / Setter
    public Long getCommentId() { return commentId; }

    public Long getOwnerId() { return ownerId; }

    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }



    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
