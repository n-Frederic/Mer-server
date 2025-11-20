package com.example.demo.entity;

import com.example.demo.repository.TeamRepository;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// ==== 新增：Jackson 注解 ====
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

@Entity
@Table(name = "user")
// 序列化时，忽略 Hibernate 代理字段；为 null 的字段不输出；默认使用 snake_case
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)


public class User {

    @Getter
    @Setter
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // id 输出为 user_id（你需要的键）
    @JsonProperty("user_id")
    private Long id;

    @Getter
    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter
    @Setter
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    // 不把密码序列化到返回 JSON
//    @JsonIgnore
    @Getter
    @Setter
    @Column(name = "password", nullable = false)
    private String password;

    @Getter
    @Setter
    @Column(name = "username")
    private String username;

    @Getter
    @Setter
    @Column(name = "phone")
    private String phone;

//    @ManyToOne
    @Getter
    @Setter
    @JoinColumn(name = "team_id", referencedColumnName = "team_id")
    // 打断可能的环（例如 Team.leader / Team.users 等），保留 set 功能
//    @JsonIgnoreProperties(value = {"leader", "users", "members"}, allowSetters = true)
    private Integer teamId;

    @Getter
    @Setter
    @JoinColumn(name = "dept_id", referencedColumnName = "dept_id")
    private Integer deptId;

    @Getter
    @Setter
    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    // 同理：防止 Role 里指回用户集合等字段造成环
//    @JsonIgnoreProperties(value = {"users", "permissions", "menus"}, allowSetters = true)
    private Integer roleId;

    @Getter
    @Setter
    @Column(name = "gender")
    private String gender;

    @Getter
    @Setter
    @Column(name = "birth_date")
    // LocalDate 按常见 yyyy-MM-dd 输出
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Getter
    @Setter
    @Column(name = "status")
    private String status;

    @Getter
    @Setter
    @Column(name = "bio")
    private String bio;

    @Getter
    @Setter
    @Column(name = "updated_at")
    // 如果也需要固定格式，可解注下一行（否则保持默认）
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    @Getter
    @Column(name = "avatar_url")
    private String avatar_url;

    @Getter
    @Setter
    @Column(name = "created_at")
    // 你的返回样例是 UTC 的 Z 结尾，这里直出为 UTC 格式
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime created_at;

    @Getter
    @Setter
    @Column(name = "last_login")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime last_login;

    public User() {}
    public User(String name, String email, String password) {
        this.name = name; this.email = email; this.password = password;
    }


}
