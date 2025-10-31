
package com.example.demo.config;

import com.example.demo.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns("/login", "/api/login","**/login","/error","/send-verification-code","/send-verification-code/","/forgot-password/**","**/forgot-password/**"); // 接口不拦截
    }
}