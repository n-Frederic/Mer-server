package com.example.demo.controller;

import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 业务 Service
    @MockBean
    private LoginService loginService;

    // 为了让 WebConfig / AuthInterceptor 不把上下文搞挂
    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        // 虽然 /login 在你的 WebConfig 里是 excludePathPatterns，稳妥起见还是让所有请求放行
        Mockito.when(authInterceptor.preHandle(any(), any(), any()))
                .thenReturn(true);
    }

    @Test
    @DisplayName("POST /login 应该调用 LoginService.login(email, password) 并返回 200")
    void login_success() throws Exception {
        // 准备一个成功的响应 DTO（使用 LoginResponseDTO 的静态工厂）
        LoginResponseDTO responseDTO =
                LoginResponseDTO.success("Test User", "user@example.com", "fake-token");

        // 按照实际签名：login(String email, String password)
        Mockito.when(loginService.login(eq("user@example.com"), eq("secret")))
                .thenReturn(responseDTO);

        String body = """
                {
                  "email": "user@example.com",
                  "password": "secret"
                }
                """;

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // 核心：验证 controller 确实从 DTO 中取参数并按顺序调用 service
        verify(loginService).login(eq("user@example.com"), eq("secret"));
    }
}
