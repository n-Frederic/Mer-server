package com.example.demo.controller;

import com.example.demo.entity.Login;
import com.example.demo.entity.User;
import com.example.demo.service.LoginService;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final LoginService loginService;

    public UserController(UserService userService, LoginService loginService) {
        this.userService = userService;
        this.loginService = loginService;
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
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userService.getUserById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    return userService.saveUser(user);
                })
                .orElseThrow(() -> new RuntimeException("用户未找到"));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }

        String token = authorizationHeader;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Optional<Login> loginRecordOpt = loginService.findByToken(token);
        if (loginRecordOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }

        Login loginRecord = loginRecordOpt.get();
        Optional<User> userOpt = userService.getUserById(loginRecord.getUser().getId());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        }

        User user = userOpt.get();
        Map<String, Object> response = Map.of(
                "ok", true,
                "user", Map.of(
                        "user_id", user.getId(),
                        "name", user.getName(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "phone", user.getPhone(),
                        "team", user.getTeam_id(),
                        "role_id", user.getRole_id(),
                        "gender", user.getGender(),
                        "birth_date", user.getBirthday(),
                        "bio", user.getBio()
                )
        );

        return ResponseEntity.ok(response);
    }
}
