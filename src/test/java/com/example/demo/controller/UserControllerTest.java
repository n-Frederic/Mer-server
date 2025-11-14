package com.example.demo.controller;

import com.example.demo.dto.UserProfileUpdateRequestDTO;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.entity.User;
import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.LoginService;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // 同样把 LoginService 和 AuthInterceptor mock 掉
    @MockBean
    private LoginService loginService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.when(authInterceptor.preHandle(any(), any(), any()))
                .thenReturn(true);
    }

    // ========== GET /user ==========

//    @Test
//    @DisplayName("GET /user 返回所有用户列表")
//    void getAllUsers_success() throws Exception {
//        User u = new User();
//        u.setId(1L);
//        u.setName("Alice");
//        u.setEmail("alice@example.com");
//        u.setCreated_at(LocalDateTime.now());
//
//        Mockito.when(userService.getAllUsers())
//                .thenReturn(List.of(u));
//
//        mockMvc.perform(get("/user"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(1))
//                .andExpect(jsonPath("$[0].name").value("Alice"));
//    }

    // ========== GET /user/{id} ==========

//    @Test
//    @DisplayName("GET /user/{id} 返回单个用户")
//    void getUser_success() throws Exception {
//        User u = new User();
//        u.setId(2L);
//        u.setName("Bob");
//
//        Mockito.when(userService.getUserById(2L))
//                .thenReturn(Optional.of(u));
//
//        mockMvc.perform(get("/user/{id}", 2L))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(2))
//                .andExpect(jsonPath("$.name").value("Bob"));
//    }

    // ========== POST /user 创建用户 ==========

    @Test
    @DisplayName("POST /user 创建用户成功，返回 ok=true")
    void createUser_success() throws Exception {
        User saved = new User();
        saved.setId(10L);
        saved.setEmail("new@example.com");

        Mockito.when(userService.saveUser(any(User.class)))
                .thenReturn(saved);

        String body = """
                {
                  "name": "New User",
                  "email": "new@example.com",
                  "password": "123456"
                }
                """;

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("用户创建成功"));
    }

    @Test
    @DisplayName("POST /user 邮箱重复触发 DataIntegrityViolationException，返回 400/错误信息")
    void createUser_emailConflict() throws Exception {
        Mockito.when(userService.saveUser(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("email"));

        String body = """
                {
                  "name": "New User",
                  "email": "dup@example.com",
                  "password": "123456"
                }
                """;

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true));
    }

    // ========== PUT /user/{id} 更新用户 ==========

//    @Test
//    @DisplayName("PUT /user/{id} 更新用户成功")
//    void updateUser_success() throws Exception {
//        User u = new User();
//        u.setId(3L);
//        u.setName("Old");
//        u.setUsername("olduser");
//        u.setEmail("old@example.com");
//        u.setPhone("123");
//        u.setTeam_id(1);
//        u.setRole_id(2);
//        u.setStatus("ACTIVE");
//        u.setUpdatedAt(LocalDateTime.now());
//
//        Mockito.when(userService.getUserById(3L))
//                .thenReturn(Optional.of(u));
//        Mockito.when(userService.saveUser(any(User.class)))
//                .thenReturn(u);
//
//        String body = """
//                {
//                  "name": "NewName",
//                  "phone": "987654"
//                }
//                """;
//
//        mockMvc.perform(put("/user/{id}", 3L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.ok").value(true))
//                .andExpect(jsonPath("$.message").value("用户信息更新成功"))
//                .andExpect(jsonPath("$.user.user_id").value(3));
//    }

    @Test
    @DisplayName("PUT /user/{id} 用户不存在，返回 404")
    void updateUser_notFound() throws Exception {
        Mockito.when(userService.getUserById(999L))
                .thenReturn(Optional.empty());

        String body = """
                {
                  "name": "NewName"
                }
                """;

        mockMvc.perform(put("/user/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    // ========== DELETE /user/{id} ==========

    @Test
    @DisplayName("DELETE /user/{id} 调用 userService.deleteUser")
    void deleteUser_success() throws Exception {
        doNothing().when(userService).deleteUser(5L);

        mockMvc.perform(delete("/user/{id}", 5L))
                .andExpect(status().isOk());
    }

    // ========== GET /user/profile ==========

    @Test
    @DisplayName("GET /user/profile 透传 UserService.getProfile 响应")
    void getProfile_success() throws Exception {
        Map<String, Object> body = Map.of("ok", true, "name", "ProfileUser");
        ResponseEntity<?> resp = ResponseEntity.ok(body);

        // 同 TaskControllerTest，用 doReturn 避免 ResponseEntity 泛型推断问题
        doReturn(resp)
                .when(userService)
                .getProfile("Bearer token-abc");

        mockMvc.perform(get("/user/profile")
                        .header("Authorization", "Bearer token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.name").value("ProfileUser"));
    }

    // ========== POST /user/logout ==========

    @Test
    @DisplayName("POST /user/logout 缺少 token，返回 400")
    void logout_missingToken() throws Exception {
        mockMvc.perform(post("/user/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("缺少 token"));
    }

    @Test
    @DisplayName("POST /user/logout 成功登出")
    void logout_success() throws Exception {
        Mockito.when(userService.logout("token-xyz"))
                .thenReturn(true);

        mockMvc.perform(post("/user/logout")
                        .header("Authorization", "Bearer token-xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    @DisplayName("POST /user/logout token 不存在或已失效")
    void logout_invalidToken() throws Exception {
        Mockito.when(userService.logout("dead-token"))
                .thenReturn(false);

        mockMvc.perform(post("/user/logout")
                        .header("Authorization", "Bearer dead-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("Token 不存在或已失效"));
    }

    // ========== PUT /user/profile ==========

    @Test
    @DisplayName("PUT /user/profile 更新个人信息成功")
    void updateProfile_success() throws Exception {
        Mockito.when(userService.updateUserProfile(
                        anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(),
                        anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(true);

        String body = """
                {
                  "name": "UserName",
                  "username": "user1",
                  "email": "u@example.com",
                  "phone": "123",
                  "gender": "M",
                  "bio": "hi",
                  "team_id": 1,
                  "role_id": 2,
                  "birth_date": "1990-01-01"
                }
                """;

        mockMvc.perform(put("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("个人信息更新成功"));
    }

    @Test
    @DisplayName("PUT /user/profile 更新失败，返回 400")
    void updateProfile_fail() throws Exception {
        Mockito.when(userService.updateUserProfile(
                        anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(),
                        anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(false);

        String body = """
                {
                  "name": "UserName",
                  "username": "user1",
                  "email": "u@example.com",
                  "phone": "123",
                  "gender": "M",
                  "bio": "hi",
                  "team_id": 1,
                  "role_id": 2,
                  "birth_date": "1990-01-01"
                }
                """;

        mockMvc.perform(put("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false));
    }
}
