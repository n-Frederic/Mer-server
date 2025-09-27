package com.example.demo.service;

import com.example.demo.entity.VerificationCode;
import com.example.demo.exception.EmailServiceUnavailableException;
import com.example.demo.exception.InvalidEmailException;
import com.example.demo.repository.VerificationCodeRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;


@Service
public class VerificationCodeService {

    private final JavaMailSender mailSender;
    private final VerificationCodeRepository repository;

    public VerificationCodeService(JavaMailSender mailSender,
                                   VerificationCodeRepository repository) {
        this.mailSender = mailSender;
        this.repository = repository;
    }

    public void sendVerificationCode(String email) {
        // 校验邮箱格式
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidEmailException();
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 保存到数据库/缓存
        VerificationCode entity = new VerificationCode(email, code, LocalDateTime.now());
        repository.save(entity);

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("注册验证码");
            message.setText("您的验证码是: " + code + "，有效期5分钟。");
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailServiceUnavailableException();
        }
    }
}

