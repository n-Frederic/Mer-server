package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.entity.PersonalTask;
import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.LoginService;
import com.example.demo.service.PersonalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonalTaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class PersonalTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonalTaskService personalTaskService;

    @MockBean
    private LoginService loginService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.when(authInterceptor.preHandle(any(), any(), any()))
                .thenReturn(true);
    }

    @Test
    @DisplayName("GET /personal-task 返回当前用户的个人任务")
    void getPersonalTask_success() throws Exception {
        Long userId = 1001L;
        List<String> tasks = List.of("Task A", "Task B");

        PersonalTask personalTask = Mockito.mock(PersonalTask.class);
        Mockito.when(personalTask.getPersonalTasks()).thenReturn(tasks);

        Mockito.when(personalTaskService.getPersonalTaskByUserId(userId))
                .thenReturn(personalTask);

        try (MockedStatic<UserContext> mocked = Mockito.mockStatic(UserContext.class)) {
            mocked.when(UserContext::getCurrentUserId).thenReturn(userId);

            mockMvc.perform(get("/personal-task"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user_id").value(userId))
                    .andExpect(jsonPath("$.personal_tasks[0]").value("Task A"))
                    .andExpect(jsonPath("$.personal_tasks[1]").value("Task B"));
        }
    }

    @Test
    @DisplayName("PUT /personal-task 更新当前用户的个人任务")
    void updatePersonalTask_success() throws Exception {
        Long userId = 1001L;
        List<String> tasks = List.of("New Task 1", "New Task 2");

        PersonalTask updated = Mockito.mock(PersonalTask.class);
        Mockito.when(updated.getPersonalTasks()).thenReturn(tasks);

        Mockito.when(personalTaskService.updatePersonalTask(eq(userId), any()))
                .thenReturn(updated);

        String body = """
                {
                  "personal_tasks": ["New Task 1", "New Task 2"]
                }
                """;

        try (MockedStatic<UserContext> mocked = Mockito.mockStatic(UserContext.class)) {
            mocked.when(UserContext::getCurrentUserId).thenReturn(userId);

            mockMvc.perform(put("/personal-task")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Personal task updated successfully"))
                    .andExpect(jsonPath("$.user_id").value(userId))
                    .andExpect(jsonPath("$.updated_personal_tasks[0]").value("New Task 1"));
        }
    }

    @Test
    @DisplayName("POST /personal-task 创建当前用户的个人任务")
    void createPersonalTask_success() throws Exception {
        Long userId = 1001L;
        List<String> tasks = List.of("Task X", "Task Y");

        PersonalTask created = Mockito.mock(PersonalTask.class);
        Mockito.when(created.getPersonalTasks()).thenReturn(tasks);

        Mockito.when(personalTaskService.createPersonalTask(eq(userId), any()))
                .thenReturn(created);

        String body = """
                {
                  "personal_tasks": ["Task X", "Task Y"]
                }
                """;

        try (MockedStatic<UserContext> mocked = Mockito.mockStatic(UserContext.class)) {
            mocked.when(UserContext::getCurrentUserId).thenReturn(userId);

            mockMvc.perform(post("/personal-task")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Personal task created successfully"))
                    .andExpect(jsonPath("$.user_id").value(userId))
                    .andExpect(jsonPath("$.created_personal_tasks[1]").value("Task Y"));
        }
    }
}
