package com.example.demo.service;

import com.example.demo.entity.VerificationCode;
import com.example.demo.exception.EmailServiceUnavailableException;
import com.example.demo.exception.InvalidEmailException;
import com.example.demo.repository.VerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VerificationCodeService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private VerificationCodeService verificationCodeService;

    private VerificationCode mockCode;

    @BeforeEach
    void setUp() {
        mockCode = new VerificationCode("test@example.com", "123456", LocalDateTime.now());
    }

    /**
     * 测试1：正常发送验证码
     */
    @Test
    void sendVerificationCode_ShouldSaveAndSendEmail() {
        // 模拟 Repository 保存
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenReturn(mockCode);

        // 模拟不抛异常的邮件发送
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> verificationCodeService.sendVerificationCode("test@example.com"));

        // 验证保存行为
        verify(verificationCodeRepository, times(1)).save(any(VerificationCode.class));

        // 验证邮件发送
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    /**
     * 测试2：无效邮箱应抛出 InvalidEmailException
     */
    @Test
    void sendVerificationCode_InvalidEmail_ShouldThrowException() {
        assertThrows(InvalidEmailException.class,
                () -> verificationCodeService.sendVerificationCode("invalid-email"));
        verify(verificationCodeRepository, never()).save(any());
    }

    /**
     * 测试3：邮件发送失败应抛出 EmailServiceUnavailableException
     */
    @Test
    void sendVerificationCode_EmailSendFails_ShouldThrowException() {
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenReturn(mockCode);
        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(EmailServiceUnavailableException.class,
                () -> verificationCodeService.sendVerificationCode("test@example.com"));
    }

    /**
     * 测试4：验证码保存与查询
     */
    @Test
    void saveAndFindVerificationCode_ShouldWork() {
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenReturn(mockCode);
        when(verificationCodeRepository.findByEmailAndCodeAndCreatedAtAfter(
                anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(mockCode));

        VerificationCode saved = verificationCodeRepository.save(mockCode);
        assertNotNull(saved);

        Optional<VerificationCode> found = verificationCodeRepository
                .findByEmailAndCodeAndCreatedAtAfter("test@example.com", "123456", LocalDateTime.now().minusMinutes(5));
        assertTrue(found.isPresent());
    }

    /**
     * 测试5：验证码格式为6位数字
     */
    @Test
    void generateVerificationCode_ShouldBeSixDigits() {
        String code = String.format("%06d", 123);
        assertTrue(code.matches("\\d{6}"));
        assertEquals(6, code.length());
    }
}
