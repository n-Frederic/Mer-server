package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RegisterService {

    private final UserRepository userRepository;

    public RegisterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RegisterResponse register(String name, String email, String password, String verificationCode) {
        // 校验验证码（这里先写死，真实环境应该查 Redis / DB / 邮件服务）
        if (!"123456".equals(verificationCode)) {
            return RegisterResponse.error();
        }

        // 检查邮箱是否已存在
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            return RegisterResponse.error();
        }

        // 保存新用户
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        User savedUser = userRepository.save(user);

        return RegisterResponse.success(savedUser);
    }

    // 内部返回类
    public static class RegisterResponse {
        private boolean ok;
        private boolean error;
        private String message;
        private String code;
        private UserData user;

        public static RegisterResponse success(User user) {
            RegisterResponse resp = new RegisterResponse();
            resp.ok = true;
            resp.error = false;
            resp.user = new UserData(user.getId(), user.getName(), user.getEmail());
            return resp;
        }

        public static RegisterResponse error() {
            RegisterResponse resp = new RegisterResponse();
            resp.ok = false;
            resp.error = true;
            resp.message = "注册信息有误";
            resp.code = "INCORRECT_REGISTRATON_INFORMATION";
            return resp;
        }

        // 内部 DTO
        public static class UserData {
            private Long id;
            private String name;
            private String email;

            public UserData(Long id, String name, String email) {
                this.id = id;
                this.name = name;
                this.email = email;
            }

            public Long getId() { return id; }
            public String getName() { return name; }
            public String getEmail() { return email; }
        }

        // Getter
        public boolean isOk() { return ok; }
        public boolean isError() { return error; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
        public UserData getUser() { return user; }
    }
}

