package com.example.demo.controller;

import com.example.demo.dto.PasswordResetRequestDTO;
import com.example.demo.dto.ResetErrorResponse;
import com.example.demo.dto.ResetSuccessResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 认证相关 API Controller
 */
@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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
            ResetSuccessResponse response = new ResetSuccessResponse("密码重置成功");
            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            // 失败返回 400 Bad Request
            // 无论内部错误码是 USER_NOT_FOUND 还是 INVALID_VERIFICATION_CODE，都按要求返回 INVALID_VERIFICATION_CODE
            ResetErrorResponse errorResponse = new ResetErrorResponse(
                    "验证信息错误，修改密码失败",
                    "INVALID_VERIFICATION_CODE"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            // 捕获其他未知异常，返回 500
            ResetErrorResponse errorResponse = new ResetErrorResponse(
                    "服务器内部错误",
                    "INTERNAL_SERVER_ERROR"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}