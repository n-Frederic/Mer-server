package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "login")
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loginId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "valid_to")
    private LocalDateTime validTo;


    // 构造函数
    public Login() {}

    public Login(User user, String token) {
        this.user = user;
        this.token = token;
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        this.validTo = LocalDateTime.now().plusHours(4);
    }

    // Getter & Setter
    public Long getLoginId() {
        return loginId;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setLoginId(Long loginId) {
        this.loginId = loginId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    // 判断 Token 是否过期
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.validTo);
    }
}
