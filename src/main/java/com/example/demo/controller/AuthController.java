package com.example.demo.controller;

import com.example.demo.dto.PasswordResetRequestDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // 💥 更改为 jakarta.validation.Valid

/**
 * 认证相关 API Controller
 */
@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // 统一成功响应体
    public static class SuccessResponse {
        public boolean ok = true;
        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }
    }

    // 统一错误响应体 (与 API 失败返回体结构一致)
    public static class ErrorResponse {
        @JsonProperty("error")
        public boolean isError = true;
        public String message;
        public String code;

        public ErrorResponse(String message, String code) {
            this.message = message;
            this.code = code;
        }
    }


    /**
     * POST <a href="http://127.0.0.1:8080/api/forgot-password/reset">...</a>
     * 验证码验证并重置密码
     */
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
        try {
            userService.resetPassword(request);

            // 成功返回 200 OK
            SuccessResponse response = new SuccessResponse("密码重置成功");
            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            // 失败返回 400 Bad Request
            // 无论内部错误码是 USER_NOT_FOUND 还是 INVALID_VERIFICATION_CODE，都按要求返回 INVALID_VERIFICATION_CODE
            ErrorResponse errorResponse = new ErrorResponse(
                    "验证信息错误，修改密码失败",
                    "INVALID_VERIFICATION_CODE"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            // 捕获其他未知异常，返回 500
            ErrorResponse errorResponse = new ErrorResponse(
                    "服务器内部错误",
                    "INTERNAL_SERVER_ERROR"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}