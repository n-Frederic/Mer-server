package com.example.demo.service;

import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.entity.Login;
import com.example.demo.entity.User;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserRepository userRepository, LoginRepository loginRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return LoginResponseDTO.error("用户不存在", "USER_NOT_FOUND");
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return LoginResponseDTO.error("密码错误", "INVALID_PASSWORD");
        }


        // 生成 token（示例使用 UUID）
        String token = UUID.randomUUID().toString();
        Login loginRecord = new Login(user, token);
        loginRepository.save(loginRecord);

        return LoginResponseDTO.success(user.getName(), user.getEmail(), token);
    }

    public Optional<Login> findByToken(String token) {
        return loginRepository.findByToken(token);
    }
}
