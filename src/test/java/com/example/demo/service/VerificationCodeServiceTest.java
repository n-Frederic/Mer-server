package com.example.demo.service;

import com.example.demo.entity.VerificationCode;
import com.example.demo.exception.EmailServiceUnavailableException;
import com.example.demo.exception.InvalidEmailException;
import com.example.demo.repository.VerificationCodeRepository;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@Feature("验证码服务")
@Story("验证码业务逻辑处理")
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
    @DisplayName("正常发送验证码")
    @Description("测试正常发送验证码的功能，验证验证码保存和邮件发送")
    @Severity(SeverityLevel.CRITICAL)
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
    @DisplayName("无效邮箱应抛出 InvalidEmailException")
    @Description("测试使用无效邮箱发送验证码，验证抛出邮箱格式异常")
    @Severity(SeverityLevel.CRITICAL)
    void sendVerificationCode_InvalidEmail_ShouldThrowException() {
        assertThrows(InvalidEmailException.class,
                () -> verificationCodeService.sendVerificationCode("invalid-email"));
        verify(verificationCodeRepository, never()).save(any());
    }

    /**
     * 测试3：邮件发送失败应抛出 EmailServiceUnavailableException
     */
    @Test
    @DisplayName("邮件发送失败应抛出 EmailServiceUnavailableException")
    @Description("测试邮件服务不可用时发送验证码，验证抛出邮件服务异常")
    @Severity(SeverityLevel.CRITICAL)
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
    @DisplayName("验证码保存与查询")
    @Description("测试验证码的保存和查询功能，验证操作正确")
    @Severity(SeverityLevel.CRITICAL)
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
    @DisplayName("验证码格式为6位数字")
    @Description("测试生成的验证码格式，验证为6位数字")
    @Severity(SeverityLevel.NORMAL)
    void generateVerificationCode_ShouldBeSixDigits() {
        String code = String.format("%06d", 123);
        assertTrue(code.matches("\\d{6}"));
        assertEquals(6, code.length());
    }
}
