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

    @Column(name = "owner_type", nullable = false)
    private String ownerType;   // task / log

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;       // 关联任务或日志

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "user_id")
    private User author;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Comment() {}

    public Comment(String ownerType, Long ownerId, User author, String content) {
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // Getter / Setter
    public Long getCommentId() { return commentId; }
    public String getOwnerType() { return ownerType; }
    public Long getOwnerId() { return ownerId; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
