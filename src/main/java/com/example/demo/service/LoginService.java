package com.example.demo.service;

import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.entity.EventLog;
import com.example.demo.entity.Login;
import com.example.demo.entity.User;
import com.example.demo.repository.EventLogRepository;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final EventLogRepository eventLogRepository;
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserRepository userRepository,EventLogRepository eventLogRepository, LoginRepository loginRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventLogRepository = eventLogRepository;
    }

    public LoginResponseDTO login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return LoginResponseDTO.error("用户不存在", "USER_NOT_FOUND");
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return LoginResponseDTO.error("密码错误", "INVALID_PASSWORD");
        }


        // 生成 token（示例使用 UUID）
        String token = UUID.randomUUID().toString();
        Login loginRecord = new Login(user, token);
        loginRepository.save(loginRecord);

        eventLogRepository.save(new EventLog(user.getId(),"LOGIN",LocalDateTime.now(),"login",user.getId()));


        return LoginResponseDTO.success(user.getName(), user.getEmail(), token);
    }
    /**
     * 登录人数时间折线图
     *
     * @param timeUnit  hour | day | week （必填）
     * @param startDate 开始日期（必填）
     * @param endDate   结束日期（必填）
     */
    public ResponseEntity<Map<String, Object>> getLoginTrend(
            String timeUnit,
            LocalDate startDate,
            LocalDate endDate
    ) {
        // 简单校验 & 规范化
        if (timeUnit == null) {
            timeUnit = "day";
        }
        timeUnit = timeUnit.toLowerCase(Locale.ROOT);
        if (!List.of("hour", "day", "week").contains(timeUnit)) {
            throw new IllegalArgumentException("timeUnit must be one of: hour, day, week");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }

        LocalDateTime startTime = startDate.atStartOfDay();
        // endDate 采用 “小于 endDate+1 天零点” 的半开区间
        LocalDateTime endTime = endDate.plusDays(1).atStartOfDay();

        List<EventLogRepository.LoginTrendProjection> projections;

        switch (timeUnit) {
            case "hour":
                projections = eventLogRepository.findLoginTrendHourly(startTime, endTime);
                break;
            case "week":
                projections = eventLogRepository.findLoginTrendWeekly(startTime, endTime);
                break;
            case "day":
            default:
                projections = eventLogRepository.findLoginTrendDaily(startTime, endTime);
                break;
        }


        // 组装 points 数组
        List<Map<String, Object>> points = new ArrayList<>();
        for (EventLogRepository.LoginTrendProjection p : projections) {
            Map<String, Object> point = new HashMap<>();
            point.put("timeBucket", p.getTimeBucket());
            point.put("loginCount", p.getLoginCount());
            point.put("uniqueUsers", p.getUniqueUsers());
            points.add(point);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timeUnit", timeUnit);
        body.put("startDate", startDate.toString());
        body.put("endDate", endDate.toString());
        body.put("points", points);

        return ResponseEntity.ok(body);
    }

    public Optional<Login> findByToken(String token) {
        return loginRepository.findByToken(token);
    }
}
