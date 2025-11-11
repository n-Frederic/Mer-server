package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserUpdateDTO {
    @JsonProperty("name")
    private String name;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("team_id")
    private Integer teamId;

    @JsonProperty("role_id")
    private Integer roleId;

    @JsonProperty("status")
    private String status;

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getStatus() {
        return status;
    }
}
