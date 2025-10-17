package com.example.demo.service;

import com.example.demo.entity.Login;
import com.example.demo.entity.User;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
//        System.out.println("user=" + user);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user_id", user.getId() != null ? user.getId() : "");
        userMap.put("name", user.getName() != null ? user.getName() : "");
        userMap.put("username", user.getUsername() != null ? user.getUsername() : "");
        userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
        userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userMap.put("team", user.getTeam_id() );
        userMap.put("role_id", user.getRole_id() );
        userMap.put("gender", user.getGender() != null ? user.getGender() : "");
        userMap.put("birth_date", user.getBirthday() != null ? user.getBirthday() : "");
        userMap.put("bio", user.getBio() != null ? user.getBio() : "");

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("user", userMap);


        return ResponseEntity.ok(response);
    }
}
