package com.example.demo.controller;

import com.example.demo.service.RegisterService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/register")
public class RegisterController {

    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @PostMapping
    public RegisterService.RegisterResponse register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");
        String verificationCode = body.get("verificationCode");

        return registerService.register(name, email, password, verificationCode);
    }
}
