package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ✅ UserService 单元测试类
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1001L);
        mockUser.setName("张三");
        mockUser.setUsername("zhangsan");
        mockUser.setEmail("zhangsan@test.com");
        mockUser.setPhone("13800000000");
        mockUser.setTeam_id(1);
        mockUser.setRole_id(4);
        mockUser.setStatus("active");
        mockUser.setPassword("encodedPassword");
    }

    /**
     * ✅ 测试1：获取所有用户成功
     */
    @Test
    void getAllUsers_Success_ShouldReturnUserList() {
        List<User> users = Arrays.asList(mockUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getName());
        verify(userRepository, times(1)).findAll();
    }

    /**
     * ✅ 测试2：根据ID获取用户（存在）
     */
    @Test
    void getUserById_UserExists_ShouldReturnUser() {
        when(userRepository.findById(1001L)).thenReturn(Optional.of(mockUser));

        Optional<User> result = userService.getUserById(1001L);

        assertTrue(result.isPresent());
        assertEquals("张三", result.get().getName());
        assertEquals("zhangsan@test.com", result.get().getEmail());
        verify(userRepository, times(1)).findById(1001L);
    }

    /**
     * ✅ 测试3：根据ID获取用户（不存在）
     */
    @Test
    void getUserById_UserNotFound_ShouldReturnEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(999L);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999L);
    }

    /**
     * ✅ 测试4：保存用户时密码被加密
     */
    @Test
    void saveUser_Success_ShouldReturnSavedUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User newUser = new User();
        newUser.setName("李四");
        newUser.setEmail("lisi@test.com");
        newUser.setPassword("plainPassword");

        User result = userService.saveUser(newUser);

        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode(eq("plainPassword"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * ✅ 测试5：删除用户
     */
    @Test
    void deleteUser_ShouldInvokeRepositoryDelete() {
        doNothing().when(userRepository).deleteById(1001L);

        userService.deleteUser(1001L);

        verify(userRepository, times(1)).deleteById(1001L);
    }

    /**
     * ✅ 测试6：更新用户资料成功
     */
    @Test
    void updateUserProfile_Success_ShouldUpdateFields() {
        when(userRepository.findByEmail("zhangsan@test.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        boolean result = userService.updateUserProfile(
                "新名字", "newusername", "zhangsan@test.com",
                "13911112222", "男", "这是简介", 2, 3, null
        );

        assertTrue(result);
        assertEquals("新名字", mockUser.getName());
        assertEquals("newusername", mockUser.getUsername());
        assertEquals("13911112222", mockUser.getPhone());
        verify(userRepository, times(1)).save(mockUser);
    }

    /**
     * ✅ 测试7：更新用户资料失败（用户不存在）
     */
    @Test
    void updateUserProfile_UserNotFound_ShouldReturnFalse() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        boolean result = userService.updateUserProfile(
                "小王", "xiaowang", "notfound@test.com",
                "12345678900", "女", "简介", 2, 3, null
        );

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }
}
