package com.example.demo.service;

import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.entity.Login;
import com.example.demo.entity.User;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LoginService单元测试
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private LoginRepository loginRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private LoginService loginService;
    
    private User mockUser;
    
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1001L);
        mockUser.setName("张三");
        mockUser.setEmail("zhangsan@test.com");
        mockUser.setPassword("$2a$10$encodedPassword");
    }
    
    /**
     * 测试1：登录成功
     */
    @Test
    void login_Success_ShouldReturnTokenAndUser() {
        when(userRepository.findByEmail("zhangsan@test.com"))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(loginRepository.save(any(Login.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        
        LoginResponseDTO result = loginService.login("zhangsan@test.com", "password123");
        
        assertNotNull(result, "返回结果不应为null");
        assertFalse(result.isError(), "登录应该成功");
        assertNotNull(result.getToken(), "应返回token");
        assertNotNull(result.getUser(), "应返回用户信息");
        assertEquals("张三", result.getUser().getName());
        
        verify(loginRepository, times(1)).save(any(Login.class));
    }
    
    /**
     * 测试2：登录失败用户不存在
     */
    @Test
    void login_UserNotFound_ShouldReturnError() {
        when(userRepository.findByEmail("notexist@test.com"))
            .thenReturn(Optional.empty());
        
        LoginResponseDTO result = loginService.login("notexist@test.com", "password");
        
        assertTrue(result.isError(), "应返回错误");
        assertEquals("USER_NOT_FOUND", result.getCode());
        assertNull(result.getToken(), "不应返回token");
        
        verify(loginRepository, never()).save(any());
    }
    
    /**
     * 测试3：登录失败密码错误
     */
    @Test
    void login_WrongPassword_ShouldReturnError() {
        when(userRepository.findByEmail("zhangsan@test.com"))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        
        LoginResponseDTO result = loginService.login("zhangsan@test.com", "wrongpassword");
        
        assertTrue(result.isError(), "应返回错误");
        assertEquals("INVALID_PASSWORD", result.getCode());
        
        verify(loginRepository, never()).save(any());
    }
    
    /**
     * 测试4：验证密码编码器被正确调用
     */
    @Test
    void login_ShouldUsePasswordEncoder() {
        when(userRepository.findByEmail("zhangsan@test.com"))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(loginRepository.save(any(Login.class))).thenAnswer(i -> i.getArgument(0));
        
        loginService.login("zhangsan@test.com", "password123");
        
        // 验证密码编码器被调用
        verify(passwordEncoder, times(1))
            .matches(eq("password123"), eq("$2a$10$encodedPassword"));
    }
    
    /**
     * 测试5：验证Repository方法调用顺序
     */
    @Test
    void login_ShouldCallRepositoriesInCorrectOrder() {
        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(loginRepository.save(any(Login.class))).thenAnswer(i -> i.getArgument(0));
        
        loginService.login("zhangsan@test.com", "password");
        
        // 验证调用顺序：先查用户，后保存登录记录
        var inOrder = inOrder(userRepository, loginRepository);
        inOrder.verify(userRepository).findByEmail(anyString());
        inOrder.verify(loginRepository).save(any(Login.class));
    }
}

