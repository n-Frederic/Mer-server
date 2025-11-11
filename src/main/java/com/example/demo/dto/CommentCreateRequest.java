package com.example.demo.dto;

public class CommentCreateRequest {

    private String ownerType;  // task / log
    private String ownerId;    // T-001 / L-001
    private String content;

    public String getOwnerType() { return ownerType; }
    public String getOwnerId() { return ownerId; }
    public String getContent() { return content; }
}
