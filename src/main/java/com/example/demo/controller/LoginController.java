package com.example.demo.controller;

import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.service.LoginService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public LoginResponseDTO login(@RequestBody LoginRequest request) {
        return loginService.login(request.getEmail(), request.getPassword());
    }

    // 内部类表示请求体
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
