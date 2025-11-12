package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long user_id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime created_at;
    private LocalDateTime last_login;
    private RoleDTO role;
    private TeamDTO team;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleDTO {
        private Integer role_id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamDTO {
        private int team_id;
        private String name;
    }
}
