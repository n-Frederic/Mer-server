package com.example.demo.controller;

import com.example.demo.dto.PasswordResetRequestDTO;
import com.example.demo.dto.ResetErrorResponse;
import com.example.demo.dto.ResetSuccessResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 手动创建 MockMvc，不加载任何安全配置
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void resetPassword_Success() throws Exception {
        // 模拟正常调用
        doNothing().when(userService).resetPassword(any(PasswordResetRequestDTO.class));

        // 创建请求对象
        PasswordResetRequestDTO request = new PasswordResetRequestDTO();
        request.setEmail("test@example.com");
        request.setVerificationCode("123456");  // 确保验证码长度为6
        request.setNewPassword("NewPass123!");

        // 执行 POST 请求
        mockMvc.perform(post("/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())  // 期望返回200 OK
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("密码重置成功"));
    }

    @Test
    void resetPassword_WithBusinessException() throws Exception {
        // 模拟验证码无效的情况
        doThrow(new BusinessException("验证信息错误，修改密码失败", "INVALID_VERIFICATION_CODE"))
                .when(userService).resetPassword(any(PasswordResetRequestDTO.class));

        // 创建请求对象
        PasswordResetRequestDTO request = new PasswordResetRequestDTO();
        request.setEmail("test@example.com");
        request.setVerificationCode("123456");
        request.setNewPassword("NewPass123!");

        // 执行 POST 请求
        mockMvc.perform(post("/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())  // 期望返回400 Bad Request
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.code").value("INVALID_VERIFICATION_CODE"))  // 错误码
                .andExpect(jsonPath("$.message").value("验证信息错误，修改密码失败"));
    }

    @Test
    void resetPassword_WithServerError() throws Exception {
        // 模拟数据库错误
        doThrow(new RuntimeException("数据库错误"))
                .when(userService).resetPassword(any(PasswordResetRequestDTO.class));

        // 创建请求对象
        PasswordResetRequestDTO request = new PasswordResetRequestDTO();
        request.setEmail("test@example.com");
        request.setVerificationCode("123456");  // 确保验证码长度为6
        request.setNewPassword("NewPass123!");

        // 执行 POST 请求
        mockMvc.perform(post("/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())  // 期望返回500 Internal Server Error
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))  // 错误码
                .andExpect(jsonPath("$.message").value("服务器内部错误"));
    }
}
