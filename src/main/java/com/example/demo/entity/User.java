package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// ==== 新增：Jackson 注解 ====
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Entity
@Table(name = "user")
// 序列化时，忽略 Hibernate 代理字段；为 null 的字段不输出；默认使用 snake_case
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // id 输出为 user_id（你需要的键）
    @JsonProperty("user_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    // 不把密码序列化到返回 JSON
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username")
    private String username;

    @Column(name = "phone")
    private String phone;

    @ManyToOne
    @JoinColumn(name = "team_id", referencedColumnName = "team_id")
    // 打断可能的环（例如 Team.leader / Team.users 等），保留 set 功能
    @JsonIgnoreProperties(value = {"leader", "users", "members"}, allowSetters = true)
    private Team team;

    @OneToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    // 同理：防止 Role 里指回用户集合等字段造成环
    @JsonIgnoreProperties(value = {"users", "permissions", "menus"}, allowSetters = true)
    private Role role;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birth_date")
    // LocalDate 按常见 yyyy-MM-dd 输出
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Column(name = "status")
    private String status;

    @Column(name = "bio")
    private String bio;

    @Column(name = "updated_at")
    // 如果也需要固定格式，可解注下一行（否则保持默认）
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    @Column(name = "avatar_url")
    private String avatar_url;

    @Column(name = "created_at")
    // 你的返回样例是 UTC 的 Z 结尾，这里直出为 UTC 格式
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime created_at;

    @Column(name = "last_login")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime last_login;

    public User() {}
    public User(String name, String email, String password) {
        this.name = name; this.email = email; this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id;}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Integer getTeam_id() { return this.team.getTeamId(); }
    public void setTeam_id(int team_id) { this.team.setTeamId(team_id); }
    public Team getTeam() { return this.team; }
    public void setTeam_id(Integer team_id) { this.team.setTeamId(team_id); }

    public Integer getRole_id() { return role.getRoleId(); }
    public void setRole_id(Integer role_id) { this.role.setRoleId(role_id); }
    public Role getRole() { return this.role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAvatar_url() { return avatar_url; }
    public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }

    public LocalDateTime getLast_login() { return last_login; }
    public void setLast_login(LocalDateTime last_login) { this.last_login = last_login; }
}
