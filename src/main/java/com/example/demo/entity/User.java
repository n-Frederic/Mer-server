package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    @Setter
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Setter
    @Column(name = "password", nullable = false)
    private String password;

    @Setter
    @Column(name = "username")
    private String username;

    @Setter
    @Column(name = "phone")
    private String phone;

    @ManyToOne
    @JoinColumn(name = "team_id", referencedColumnName = "team_id")
    private Team team;

    @OneToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    private Role role;

    @Setter
    @Column(name = "gender")
    private String gender;

    @Setter
    @Column(name = "birth_date")
//    private LocalDateTime birthday;
    private LocalDate birthday;

    @Column(name = "status")
    private String status;

    @Setter
    @Column(name = "bio")
    private String bio;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "avatar_url")
    private String avatar_url;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "last_login")
    private LocalDateTime last_login;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }


    public String getGender() {
        return gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getBio() {
        return bio;
    }

    public Integer getTeam_id() {
        return this.team.getTeamId();
    }

    public void setTeam_id(int team_id) {
        this.team.setTeamId(team_id);
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam_id(Integer team_id) {
        this.team.setTeamId(team_id);
    }

    public Integer getRole_id() {
        return role.getRoleId();
    }

    public void setRole_id(Integer role_id) {
        this.role.setRoleId(role_id);
    }

    public Role getRole() {
        return this.role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getLast_login() {
        return last_login;
    }

    public void setLast_login(LocalDateTime last_login) {
        this.last_login = last_login;
    }
}
