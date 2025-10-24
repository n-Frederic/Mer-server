package com.example.demo.service;

import com.example.demo.dto.UserProfileUpdateRequestDTO;
import com.example.demo.entity.Login;
import com.example.demo.entity.User;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.*;

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
    @Transactional
    public boolean logout(String token) {
        Optional<Login> loginOpt = loginRepository.findByToken(token);

        if (loginOpt.isPresent()) {
            // 方式1: 直接删除记录
            loginRepository.delete(loginOpt.get());
            return true;
        }

        return false;
    }
    /**
     * 登出 - 标记 token 为过期（如果你想保留记录）
     */


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

        Map<String, Object> userMap = new LinkedHashMap<>();
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


    public boolean updateUserProfile(String name, String username, String email, String phone, String gender, String bio, int team_id, int role_id, LocalDateTime birthday) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setGender(gender);
        user.setBio(bio);
        user.setTeam_id(team_id);
        user.setRole_id(role_id);

        if (birthday != null) {
            user.setBirthday(birthday);
        }

        userRepository.save(user);
        return true;
    }

}
