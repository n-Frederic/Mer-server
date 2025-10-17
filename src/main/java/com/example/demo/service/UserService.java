package com.example.demo.service;

import com.example.demo.entity.Login;
import com.example.demo.entity.User;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final LoginRepository loginRepository;

    public UserService(UserRepository userRepository, LoginRepository loginRepository) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public ResponseEntity<?> getProfile(String authorizationHeader) {

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }

        String token = authorizationHeader;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Optional<Login> loginRecordOpt = loginRepository.findByToken(token);
        if (loginRecordOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }

        Login loginRecord = loginRecordOpt.get();
        Optional<User> userOpt = getUserById(loginRecord.getUser().getId());

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
