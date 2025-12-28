package com.example.demo.service;

import com.example.demo.dto.PasswordResetRequestDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.EventLogRepository;
import com.example.demo.repository.LoginRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VerificationCodeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventLogRepository eventLogRepository;

    @Value("${verification.code.validity.minutes:10}")
    private long validityMinutes;

    public UserService(UserRepository userRepository, EventLogRepository eventLogRepository, LoginRepository loginRepository,VerificationCodeRepository verificationCodeRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.eventLogRepository = eventLogRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public boolean logout(String token) {
        Optional<Login> loginOpt = loginRepository.findByToken(token);

        if (loginOpt.isPresent()) {
            // 方式1: 直接删除记录
            loginRepository.delete(loginOpt.get());
            return true;
        }

        return false;
    }
    /**
     * 登出 - 标记 token 为过期（如果你想保留记录）
     */

    @Transactional
    public void resetPassword(PasswordResetRequestDTO request) throws BusinessException {
        String email = request.getEmail();
        String code = request.getVerificationCode();
        String newPassword = request.getNewPassword();

        User user = userRepository.findByEmail(email)
                // 隐藏用户存在性，返回通用错误码
                .orElse(null);

        // 1. 验证码校验
        LocalDateTime expirationTimeLimit = LocalDateTime.now().minusMinutes(validityMinutes);

        Optional<VerificationCode> codeOptional = verificationCodeRepository
                .findByEmailAndCodeAndCreatedAtAfter(email, code, expirationTimeLimit);

        if (codeOptional.isEmpty() || user == null) {
            // 统一返回失败响应
            throw new BusinessException("验证信息错误，修改密码失败", "INVALID_VERIFICATION_CODE");
        }

        // 2. 更新密码：使用 PasswordEncoder 加密
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
        eventLogRepository.save(new EventLog(user.getId(),"RESET PASSWORD",LocalDateTime.now(),"user",user.getId()));


        // 3. 删除已使用的验证码
        verificationCodeRepository.deleteByEmail(email);
    }


    public ResponseEntity<?> getProfile(String authorizationHeader) {

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }

        String token = authorizationHeader;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Optional<Login> loginRecordOpt = loginRepository.findByToken(token);
        if (loginRecordOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }

        Login loginRecord = loginRecordOpt.get();
        Optional<User> userOpt = getUserById(loginRecord.getUser().getId());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        }

        User user = userOpt.get();
//        System.out.println("user=" + user);

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("user_id", user.getId() != null ? user.getId() : "");
        userMap.put("name", user.getName() != null ? user.getName() : "");
        userMap.put("username", user.getUsername() != null ? user.getUsername() : "");
        userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
        userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userMap.put("team", user.getTeamId() );
        userMap.put("role_id", user.getRoleId() );
        userMap.put("gender", user.getGender() != null ? user.getGender() : "");
        userMap.put("birth_date", user.getBirthday() != null ? user.getBirthday() : "");
        userMap.put("bio", user.getBio() != null ? user.getBio() : "");

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("user", userMap);


        return ResponseEntity.ok(response);
    }


    // 1. 修改方法签名：将 int team_id, int roleId 改为 Integer (允许为 null)
    public boolean updateUserProfile(String name, String username, String email, String phone, String gender, String bio, Integer team_id, Integer roleId, LocalDate birthday) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        // 对于 String 类型，建议也加上判空，实现“只更新已修改的字段”
        // 如果前端传 null，就不覆盖数据库里的原值
        if (name != null) user.setName(name);
        if (username != null) user.setUsername(username);
        // email 通常作为查找依据不轻易修改，但如果业务允许改：
        if (email != null) user.setEmail(email);
        if (phone != null) user.setPhone(phone);
        if (gender != null) user.setGender(gender);
        if (bio != null) user.setBio(bio);

        // 只有当前端真的传了 team_id (不为 null) 时，才去更新数据库
        if (team_id != null) {
            user.setTeamId(team_id);
        }

        if (roleId != null) {
            user.setRoleId(roleId);
        }

        if (birthday != null) {
            user.setBirthday(birthday);
        }

        userRepository.save(user);
        return true;
    }

    public ResponseEntity<Map<String, Object>> getResetPasswordAlerts(LocalDate date) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        int threshold = 5;

        List<EventLogRepository.ResetPasswordAlertProjection> projections =
                eventLogRepository.findResetPasswordAlerts(queryDate);

        // 组装 users 数组
        List<Map<String, Object>> users = projections.stream().map(p -> {
            Map<String, Object> u = new HashMap<>();
            u.put("userId", p.getUserId());
            u.put("userName", p.getUserName());
            u.put("resetCount", p.getResetCount());
            return u;
        }).toList();

        // 组装响应 body
        Map<String, Object> body = new HashMap<>();
        body.put("date", queryDate.toString());
        body.put("threshold", threshold);
        body.put("totalUsers", users.size());
        body.put("users", users);

        return ResponseEntity.ok(body);
    }
}
