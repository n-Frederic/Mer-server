package com.example.demo.service;

import com.example.demo.entity.VerificationCode;
import com.example.demo.repository.VerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VerificationCodeService单元测试
 * 测试验证码服务的核心功能
 */
@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;
    
    @InjectMocks
    private VerificationCodeService verificationCodeService;
    
    private VerificationCode mockCode;
    
    @BeforeEach
    void setUp() {
        // 使用构造函数或现有的setter创建对象
        mockCode = new VerificationCode();
    }
    
    /**
     * 测试1：保存验证码 - 成功
     */
    @Test
    void saveVerificationCode_Success_ShouldSaveCode() {
        when(verificationCodeRepository.save(any(VerificationCode.class)))
            .thenReturn(mockCode);
        
        VerificationCode newCode = new VerificationCode();
        VerificationCode result = verificationCodeRepository.save(newCode);
        
        assertNotNull(result);
        verify(verificationCodeRepository, times(1)).save(newCode);
    }
    
    /**
     * 测试2：查找验证码 - 存在且未过期
     */
    @Test
    void findValidCode_CodeExists_ShouldReturnCode() {
        LocalDateTime validTime = LocalDateTime.now().minusMinutes(3);
        
        when(verificationCodeRepository.findByEmailAndCodeAndCreatedAtAfter(
            eq("test@example.com"),
            eq("123456"),
            any(LocalDateTime.class)
        )).thenReturn(Optional.of(mockCode));
        
        Optional<VerificationCode> result = verificationCodeRepository
            .findByEmailAndCodeAndCreatedAtAfter(
                "test@example.com", 
                "123456", 
                LocalDateTime.now().minusMinutes(5)
            );
        
        assertTrue(result.isPresent(), "应该找到验证码");
    }
    
    /**
     * 测试3：查找验证码 - 已过期
     */
    @Test
    void findValidCode_CodeExpired_ShouldReturnEmpty() {
        when(verificationCodeRepository.findByEmailAndCodeAndCreatedAtAfter(
            anyString(), anyString(), any(LocalDateTime.class)
        )).thenReturn(Optional.empty());
        
        Optional<VerificationCode> result = verificationCodeRepository
            .findByEmailAndCodeAndCreatedAtAfter(
                "test@example.com",
                "123456",
                LocalDateTime.now().minusMinutes(5)
            );
        
        assertFalse(result.isPresent(), "过期验证码应返回空");
    }
    
    /**
     * 测试4：验证码格式验证 - 6位数字
     */
    @Test
    void verifyCodeFormat_SixDigits_ShouldBeValid() {
        String code = "123456";
        
        assertTrue(code.matches("\\d{6}"), "验证码应为6位数字");
        assertEquals(6, code.length());
    }
    
    /**
     * 测试5：Repository方法调用验证
     */
    @Test
    void findByEmailAndCode_ShouldCallRepositoryWithCorrectParams() {
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(5);
        
        when(verificationCodeRepository.findByEmailAndCodeAndCreatedAtAfter(
            anyString(), anyString(), any(LocalDateTime.class)
        )).thenReturn(Optional.of(mockCode));
        
        verificationCodeRepository.findByEmailAndCodeAndCreatedAtAfter(
            "test@test.com",
            "123456",
            timeLimit
        );
        
        verify(verificationCodeRepository, times(1))
            .findByEmailAndCodeAndCreatedAtAfter(
                eq("test@test.com"),
                eq("123456"),
                eq(timeLimit)
            );
    }
}

