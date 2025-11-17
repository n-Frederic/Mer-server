package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AssigneeDTO {

    @JsonProperty("user_id")   // 保证序列化成 user_id
    private Long userId;

    private String name;

    public AssigneeDTO(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
