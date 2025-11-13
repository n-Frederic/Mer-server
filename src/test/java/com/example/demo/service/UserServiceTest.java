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
 * UserService单元测试
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
    }
    
    /**
     * 测试1：获取所有用户成功
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
     * 测试2：根据ID获取用户用户存在
     */
    @Test
    void getUserById_UserExists_ShouldReturnUser() {
        when(userRepository.findById(1001L)).thenReturn(Optional.of(mockUser));
        
        Optional<User> result = userService.getUserById(1001L);
        
        assertTrue(result.isPresent(), "用户应该存在");
        assertEquals("张三", result.get().getName());
        assertEquals("zhangsan@test.com", result.get().getEmail());
        
        verify(userRepository, times(1)).findById(1001L);
    }
    
    /**
     * 测试3：根据ID获取用户用户不存在
     */
    @Test
    void getUserById_UserNotFound_ShouldReturnEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        Optional<User> result = userService.getUserById(999L);
        
        assertFalse(result.isPresent(), "应返回空Optional");
        
        verify(userRepository, times(1)).findById(999L);
    }
    
    /**
     * 测试4：保存用户成功
     */
    @Test
    void saveUser_Success_ShouldReturnSavedUser() {
        // Mock passwordEncoder的encode方法
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        
        User newUser = new User();
        newUser.setName("李四");
        newUser.setEmail("lisi@test.com");
        newUser.setPassword("plainPassword");
        
        User result = userService.saveUser(newUser);
        
        assertNotNull(result);
        
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }
    
    /**
     * 测试5：根据邮箱查找用户成功
     */
    @Test
    void findByEmail_UserExists_ShouldReturnUser() {
        when(userRepository.findByEmail("zhangsan@test.com"))
            .thenReturn(Optional.of(mockUser));
        
        Optional<User> result = userRepository.findByEmail("zhangsan@test.com");
        
        assertTrue(result.isPresent());
        assertEquals("张三", result.get().getName());
    }
}

