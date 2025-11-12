package com.example.demo.controller;

import com.example.demo.config.TestSecurityConfig;
import com.example.demo.dto.PasswordResetRequestDTO;
import com.example.demo.entity.Login;
import com.example.demo.entity.User;
import com.example.demo.service.LoginService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ActiveProfiles("test")

@Import({TestSecurityConfig.class})
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // 用于序列化请求体

    @MockBean
    private UserService userService; // 业务逻辑依赖

    // 移除不需要的LoginService依赖（因为接口不需要token验证）
    @MockBean
    private LoginService loginService; // 仅保留避免上下文错误，实际测试中不使用



    // 测试场景：业务逻辑异常（如验证码错误）
    @Test
    void resetPassword_WithBusinessException() throws Exception {
        // 移除token相关模拟代码（接口不需要token验证）

        // 模拟业务逻辑抛出异常（如验证码无效）
        doThrow(new com.example.demo.exception.BusinessException("无效验证码"))
                .when(userService).resetPassword(any(PasswordResetRequestDTO.class));

        PasswordResetRequestDTO request = new PasswordResetRequestDTO();
        request.setEmail("test@example.com");
        request.setVerificationCode("wrong-code");
        request.setNewPassword("NewPass123!");

        mockMvc.perform(post("/api/forgot-password/reset")
                        // 移除token请求头
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_VERIFICATION_CODE"))
                .andExpect(jsonPath("$.message").value("验证信息错误，修改密码失败"));
    }

    // 补充测试场景：密码重置成功
    @Test
    void resetPassword_Success() throws Exception {
        // 模拟业务逻辑执行成功
        doNothing().when(userService).resetPassword(any(PasswordResetRequestDTO.class));

        PasswordResetRequestDTO request = new PasswordResetRequestDTO();
        request.setEmail("test@example.com");
        request.setVerificationCode("valid-code");
        request.setNewPassword("NewPass123!");

        mockMvc.perform(post("/api/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码重置成功"));
    }

    // 补充测试场景：服务器内部异常
    @Test
    void resetPassword_WithServerError() throws Exception {
        // 模拟抛出未知异常
        doThrow(new RuntimeException("数据库错误"))
                .when(userService).resetPassword(any(PasswordResetRequestDTO.class));

        PasswordResetRequestDTO request = new PasswordResetRequestDTO();
        request.setEmail("test@example.com");
        request.setVerificationCode("valid-code");
        request.setNewPassword("NewPass123!");

        mockMvc.perform(post("/api/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("服务器内部错误"));
    }
}
