package com.example.demo.interceptor;

import com.example.demo.context.UserContext;
import com.example.demo.entity.Login;
import com.example.demo.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final LoginService loginService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(LoginService loginService, ObjectMapper objectMapper) {
        this.loginService = loginService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {


        // 调试信息
        System.out.println("=".repeat(50));
        System.out.println("请求 URI: " + request.getRequestURI());
        System.out.println("请求 Method: " + request.getMethod());
        System.out.println("Context Path: " + request.getContextPath());
        System.out.println("Servlet Path: " + request.getServletPath());
        System.out.println("=".repeat(50));

        // 1. 从请求头获取 token
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        // 2. 验证 token
        if (token == null || token.isEmpty()) {
            return sendErrorResponse(response, 401, "TOKEN_MISSING", "缺少认证 token");
        }

        // 3. 根据 token 查询登录记录
        Optional<Login> loginOpt = loginService.findByToken(token);

        if (loginOpt.isEmpty()) {
            return sendErrorResponse(response, 401, "INVALID_TOKEN", "无效的 token");
        }

        Login login = loginOpt.get();

        // 4. 检查 token 是否过期
        if (login.isExpired()) {
            return sendErrorResponse(response, 401, "TOKEN_EXPIRED", "Token 已过期，请重新登录");
        }

        // 5. Token 有效，将用户ID存入 ThreadLocal
        UserContext.setCurrentUserId(login.getUser().getId());

        return true; // 继续执行
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 请求结束后清除 ThreadLocal
        UserContext.clear();
    }

    // 发送错误响应
    private boolean sendErrorResponse(HttpServletResponse response,
                                      int status,
                                      String errorCode,
                                      String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        return false;
    }
}