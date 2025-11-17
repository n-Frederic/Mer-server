package com.example.demo.dto;

import jakarta.persistence.criteria.CriteriaBuilder;

public class CommentCreateRequest {


    private Long ownerId;    // T-001 / L-001
    private Long logId;
    private String content;

    public Long getLogId() {
        return logId;
    }

    public Long getOwnerId() { return ownerId; }
    public String getContent() { return content; }
}
