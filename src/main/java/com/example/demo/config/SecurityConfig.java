package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置类，用于提供 PasswordEncoder Bean 和配置 Spring Security
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 Spring Security 的安全过滤器链
     * 用于控制哪些接口需要认证，哪些接口可以公开访问
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF 保护（因为是 RESTful API，使用 token 认证，不需要 CSRF）
            .csrf(csrf -> csrf.disable())

            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许所有请求通过 Spring Security
                // 实际的认证由 AuthInterceptor 处理
                .anyRequest().permitAll()
            )

            // 配置会话管理策略为无状态（适合 token 认证方式）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 禁用 HTTP Basic 认证
            .httpBasic(basic -> basic.disable())

            // 禁用表单登录
            .formLogin(form -> form.disable());

        return http.build();
    }
}