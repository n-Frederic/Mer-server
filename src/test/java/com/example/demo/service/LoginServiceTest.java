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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ✅ LoginService 单元测试
 *
 * 测试目标：
 * - 登录成功返回正确信息
 * - 用户不存在时返回错误
 * - 密码错误时返回错误
 * - 验证 PasswordEncoder 调用
 * - 验证调用顺序
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
     * ✅ 测试1：登录成功
     */
    @Test
    void login_Success_ShouldReturnTokenAndUser() {
        when(userRepository.findByEmail("zhangsan@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(loginRepository.save(any(Login.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponseDTO result = loginService.login("zhangsan@test.com", "password123");

        assertNotNull(result);
        assertFalse(result.isError(), "登录应成功");
        assertNotNull(result.getToken(), "应生成token");
        assertNotNull(result.getUser(), "应返回用户信息");
        assertEquals("张三", result.getUser().getName());

        verify(loginRepository, times(1)).save(any(Login.class));
    }

    /**
     * ✅ 测试2：用户不存在
     */
    @Test
    void login_UserNotFound_ShouldReturnError() {
        when(userRepository.findByEmail("notexist@test.com")).thenReturn(Optional.empty());

        LoginResponseDTO result = loginService.login("notexist@test.com", "password");

        assertTrue(result.isError(), "应返回错误");
        assertEquals("USER_NOT_FOUND", result.getCode());
        assertNull(result.getToken());

        verify(loginRepository, never()).save(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * ✅ 测试3：密码错误
     */
    @Test
    void login_WrongPassword_ShouldReturnError() {
        when(userRepository.findByEmail("zhangsan@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginResponseDTO result = loginService.login("zhangsan@test.com", "wrongpassword");

        assertTrue(result.isError());
        assertEquals("INVALID_PASSWORD", result.getCode());
        assertNull(result.getToken());

        verify(loginRepository, never()).save(any());
    }

    /**
     * ✅ 测试4：验证密码编码器调用
     */
    @Test
    void login_ShouldUsePasswordEncoder() {
        when(userRepository.findByEmail("zhangsan@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(loginRepository.save(any(Login.class))).thenAnswer(i -> i.getArgument(0));

        loginService.login("zhangsan@test.com", "password123");

        verify(passwordEncoder, times(1)).matches(eq("password123"), eq("$2a$10$encodedPassword"));
    }

    /**
     * ✅ 测试5：验证 Repository 调用顺序
     */
    @Test
    void login_ShouldCallRepositoriesInCorrectOrder() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(loginRepository.save(any(Login.class))).thenAnswer(i -> i.getArgument(0));

        loginService.login("zhangsan@test.com", "password");

        var inOrder = inOrder(userRepository, loginRepository);
        inOrder.verify(userRepository).findByEmail(anyString());
        inOrder.verify(loginRepository).save(any(Login.class));
    }

    /**
     * ✅ 测试6：边界情况 - 空邮箱或密码
     */
    @Test
    void login_EmptyEmailOrPassword_ShouldReturnError() {
        // 空邮箱
        LoginResponseDTO result1 = loginService.login("", "password");
        assertTrue(result1.isError());

        // 空密码
        LoginResponseDTO result2 = loginService.login("zhangsan@test.com", "");
        assertTrue(result2.isError());
    }

    /**
     * ✅ 测试7：token 生成唯一性
     */
    @Test
    void login_ShouldGenerateDifferentTokensForDifferentLogins() {
        when(userRepository.findByEmail("zhangsan@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(loginRepository.save(any(Login.class))).thenAnswer(i -> i.getArgument(0));

        LoginResponseDTO result1 = loginService.login("zhangsan@test.com", "password123");
        LoginResponseDTO result2 = loginService.login("zhangsan@test.com", "password123");

        assertNotEquals(result1.getToken(), result2.getToken(), "两次登录应生成不同token");
    }
}
