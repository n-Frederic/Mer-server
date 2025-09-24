package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {
    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return LoginResponse.error("用户不存在", "USER_NOT_FOUND");
        }

        User user = optionalUser.get();
        if (!user.getPassword().equals(password)) {
            return LoginResponse.error("密码错误", "INVALID_PASSWORD");
        }

        // 生成 token（示例使用 UUID）
        String token = UUID.randomUUID().toString();

        return LoginResponse.success(user.getName(), user.getEmail(), token);
    }

    // 内部静态类表示返回对象
    public static class LoginResponse {
        private boolean error;
        private String message;
        private String code;
        private UserData user;
        private String token;

        // 成功返回
        public static LoginResponse success(String name, String email, String token) {
            LoginResponse resp = new LoginResponse();
            resp.error = false;
            resp.user = new UserData(name, email);
            resp.token = token;
            return resp;
        }

        // 失败返回
        public static LoginResponse error(String message, String code) {
            LoginResponse resp = new LoginResponse();
            resp.error = true;
            resp.message = message;
            resp.code = code;
            return resp;
        }

        // Getter
        public boolean isError() { return error; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
        public UserData getUser() { return user; }
        public String getToken() { return token; }

        // 内部类
        public static class UserData {
            private String name;
            private String email;

            public UserData(String name, String email) {
                this.name = name;
                this.email = email;
            }

            public String getName() { return name; }
            public String getEmail() { return email; }
        }
    }
}
