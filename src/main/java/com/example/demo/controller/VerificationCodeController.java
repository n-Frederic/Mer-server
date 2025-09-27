package com.example.demo.controller;

import com.example.demo.exception.EmailServiceUnavailableException;
import com.example.demo.exception.InvalidEmailException;
import com.example.demo.service.VerificationCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/send-verification-code")
public class VerificationCodeController {

    private final VerificationCodeService verificationCodeService;

    public VerificationCodeController(VerificationCodeService verificationCodeService) {
        this.verificationCodeService = verificationCodeService;
    }

    @PostMapping("/")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            verificationCodeService.sendVerificationCode(email);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "验证码已发送"
            ));
        } catch (InvalidEmailException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", "邮箱不存在或邮箱不可用",
                    "code", "INVALID_EMAIL_FORMAT"
            ));
        } catch (EmailServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "error", true,
                    "message", "邮件发送服务暂时不可用，请稍后再试",
                    "code", "EMAIL_SERVICE_UNAVAILABLE"
            ));
        }
    }
}
