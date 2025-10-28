package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.UserProfileUpdateRequestDTO;
import com.example.demo.entity.User;
import com.example.demo.service.LoginService;
import com.example.demo.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            userService.saveUser(user);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "用户创建成功"
            ));
        } catch (DataIntegrityViolationException e) {
            String message;
            String code;

            if (e.getMessage().contains("email")) {
                message = "邮箱已存在";
                code = "EMAIL_ALREADY_EXISTS";
            } else if (e.getMessage().contains("username")) {
                message = "用户名已存在";
                code = "USERNAME_ALREADY_EXISTS";
            } else {
                message = "数据不合法，违反数据库约束";
                code = "DATA_INTEGRITY_VIOLATION";
            }

            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", message,
                    "code", code
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", true,
                    "message", "用户创建失败：" + e.getMessage(),
                    "code", "INTERNAL_SERVER_ERROR"
            ));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            return userService.getUserById(id)
                    .map(user -> {
                        user.setName(userDetails.getName());
                        user.setEmail(userDetails.getEmail());
                        userService.saveUser(user);
                        return ResponseEntity.ok(Map.of(
                                "ok", true,
                                "message", "用户更新成功"
                        ));
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                            "error", true,
                            "message", "用户未找到",
                            "code", "USER_NOT_FOUND"
                    )));
        } catch (DataIntegrityViolationException e) {
            String message;
            String code;

            if (e.getMessage().contains("email")) {
                message = "邮箱已被其他用户使用";
                code = "EMAIL_ALREADY_EXISTS";
            } else if (e.getMessage().contains("username")) {
                message = "用户名已被其他用户使用";
                code = "USERNAME_ALREADY_EXISTS";
            } else {
                message = "数据不合法，违反数据库约束";
                code = "DATA_INTEGRITY_VIOLATION";
            }

            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", message,
                    "code", code
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", true,
                    "message", "用户更新失败：" + e.getMessage(),
                    "code", "INTERNAL_SERVER_ERROR"
            ));
        }
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return userService.getProfile(authorizationHeader);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 从 Authorization header 提取 token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("ok", false);
                response.put("message", "缺少 token");
                return ResponseEntity.badRequest().body(response);
            }

            String token = authHeader.substring(7);

            // 删除 token（失效登录）
            boolean success = userService.logout(token);

            if (success) {
                response.put("ok", true);
                response.put("message", "登出成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("ok", false);
                response.put("message", "Token 不存在或已失效");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("ok", false);
            response.put("message", "登出失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile( @RequestBody UserProfileUpdateRequestDTO request) {
        String name = request.getName();
        String username = request.getUsername();
        String email = request.getEmail();
        String phone = request.getPhone();
        String gender = request.getGender();
        String bio = request.getBio();
        int team_id = request.getTeam_id();
        int role_id = request.getRole_id();
        LocalDateTime birthday = request.getBirth_date();
        boolean ok = userService.updateUserProfile(name,username,email,phone,gender,bio,team_id,role_id,birthday);

        if (ok) {
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "个人信息更新成功"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "error", "Invalid data or unauthorized"
            ));
        }
    }
}
