package com.example.demo.controller;

import com.example.demo.dto.TaskCreateDTO;
import com.example.demo.dto.TaskProgressUpdateDTO;
import com.example.demo.dto.TaskUpdateRequestDTO;
import com.example.demo.entity.Task;
import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.LoginService;
import com.example.demo.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 业务 Service
    @MockBean
    private TaskService taskService;

    // 为了解决 WebConfig / AuthInterceptor 依赖 LoginService 的问题：
    @MockBean
    private LoginService loginService;

    // 拦截器本身也 mock 掉，避免 401 拦截所有请求
    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        // 让拦截器一律放行，否则默认 boolean mock 返回 false，会拦截所有请求
        Mockito.when(authInterceptor.preHandle(any(), any(), any()))
                .thenReturn(true);
    }

    // ========== POST /tasks 创建任务 ==========

    @Test
    @DisplayName("POST /tasks 创建任务成功，返回 ok=true 和 taskId")
    void createTask_success() throws Exception {
        ResponseEntity<?> responseEntity =
                ResponseEntity.ok(Map.of("ok", true, "taskId", 123L));

        doReturn(responseEntity)
                .when(taskService)
                .createTask(any(TaskCreateDTO.class), any());

        String body = """
                {
                  "title": "Test Task"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.taskId").value(123));
    }

    // ========== GET /tasks/personal 个人任务列表 ==========

    @Test
    @DisplayName("GET /tasks/personal 返回分页个人任务列表")
    void getPersonalTasks() throws Exception {
        Mockito.when(taskService.getPersonalTasks(
                        any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(
                        Map.of(
                                "list", List.of(),
                                "page", 1,
                                "pageSize", 10
                        )
                );

        mockMvc.perform(get("/tasks/personal")
                        .param("status", "OPEN")
                        .param("priority", "HIGH")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    // ========== GET /tasks/myView 个人视图任务列表 ==========

    @Test
    @DisplayName("GET /tasks/myView 返回个人视图任务列表")
    void getViewTasks() throws Exception {
        Mockito.when(taskService.getViewTasks(
                        any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(
                        Map.of(
                                "list", List.of(),
                                "page", 2,
                                "pageSize", 5
                        )
                );

        mockMvc.perform(get("/tasks/myView")
                        .param("status", "OPEN")
                        .param("priority", "MEDIUM")
                        .param("page", "2")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.pageSize").value(5));
    }

    // ========== GET /tasks/assignees 可分配员工列表 ==========

    @Test
    @DisplayName("GET /tasks/assignees 返回可分配员工列表")
    void getAssignableUsers() throws Exception {
        Mockito.when(taskService.getAssignees(
                        any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(
                        Map.of(
                                "list", List.of(
                                        Map.of("userId", 1001L, "name", "Alice")
                                ),
                                "total", 1
                        )
                );

        mockMvc.perform(get("/tasks/assignees")
                        .param("keyword", "Ali")
                        .param("department_id", "1")
                        .param("team_id", "2")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].userId").value(1001));
    }

    // ========== GET /tasks/all 全部任务 ==========

    @Test
    @DisplayName("GET /tasks/all 返回所有任务")
    void getAllTasks() throws Exception {
        Mockito.when(taskService.getAllTasks(any(), any(), anyInt(), anyInt()))
                .thenReturn(
                        Map.of(
                                "list", List.of(
                                        Map.of("id", 1L, "title", "T1")
                                ),
                                "page", 1,
                                "pageSize", 20
                        )
                );

        mockMvc.perform(get("/tasks/all")
                        .param("status", "OPEN")
                        .param("priority", "LOW")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].id").value(1))
                .andExpect(jsonPath("$.page").value(1));
    }

    // ========== GET /tasks/{taskId} 任务详情 ==========

    @Test
    @DisplayName("GET /tasks/{taskId} 返回任务详情")
    void getTaskById() throws Exception {
        Mockito.when(taskService.getTaskById(1L))
                .thenReturn(
                        Map.of(
                                "ok", true,
                                "task", Map.of(
                                        "id", 1L,
                                        "title", "Detail Task"
                                )
                        )
                );

        mockMvc.perform(get("/tasks/{taskId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.task.id").value(1));
    }

    // ========== PUT /tasks/{taskId} 更新任务信息（成功） ==========

    @Test
    @DisplayName("PUT /tasks/{taskId} 更新任务成功，返回 code=200")
    void updateTask_success() throws Exception {
        Task task = Mockito.mock(Task.class);
        Mockito.when(task.getId()).thenReturn(1L);
        Mockito.when(task.getTitle()).thenReturn("Updated");
        Mockito.when(task.getDescription()).thenReturn("Updated desc");
        Mockito.when(task.getPriority()).thenReturn("HIGH");
        Mockito.when(task.getStatus()).thenReturn("OPEN");
        Mockito.when(task.getStartAt()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
        Mockito.when(task.getDueAt()).thenReturn(Instant.parse("2025-02-01T00:00:00Z"));
        Mockito.when(task.getUpdatedAt()).thenReturn(Instant.parse("2025-01-02T00:00:00Z"));
        Mockito.when(task.getParent_task()).thenReturn(null);

        Mockito.when(taskService.updateTaskInfo(eq("T-1"), any(TaskUpdateRequestDTO.class)))
                .thenReturn(task);

        String body = """
                {
                  "title": "Updated",
                  "description": "Updated desc",
                  "priority": "HIGH",
                  "status": "OPEN",
                  "tags": []
                }
                """;

        mockMvc.perform(put("/tasks/{taskId}", "T-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("任务更新成功"))
                .andExpect(jsonPath("$.data.taskId").value("T-1"));
    }

    // ========== PUT /tasks/{taskId} 更新任务失败（404） ==========

    @Test
    @DisplayName("PUT /tasks/{taskId} 更新任务失败，返回 404 和错误信息")
    void updateTask_notFound() throws Exception {
        Mockito.when(taskService.updateTaskInfo(eq("T-999"), any(TaskUpdateRequestDTO.class)))
                .thenThrow(new RuntimeException("任务不存在"));

        String body = """
                {
                  "title": "Updated",
                  "description": "Updated desc"
                }
                """;

        mockMvc.perform(put("/tasks/{taskId}", "T-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ========== PUT /tasks/{taskId}/progress 更新进度（成功） ==========

    @Test
    @DisplayName("PUT /tasks/{taskId}/progress 进度更新成功")
    void updateProgress_success() throws Exception {
        doNothing().when(taskService).updateTaskProgress(any(TaskProgressUpdateDTO.class));

        String body = """
                {
                  "taskId": "T-1",
                  "progressPct": 80
                }
                """;

        mockMvc.perform(put("/tasks/{taskId}/progress", "T-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("进度更新成功"));
    }

    // ========== PUT /tasks/{taskId}/progress 进度非法（服务抛 IllegalArgumentException） ==========

    @Test
    @DisplayName("PUT /tasks/{taskId}/progress 进度非法时返回 400")
    void updateProgress_invalid() throws Exception {
        doThrow(new IllegalArgumentException("进度百分比必须在 0-100 范围内"))
                .when(taskService).updateTaskProgress(any(TaskProgressUpdateDTO.class));

        String body = """
                {
                  "taskId": "T-1",
                  "progressPct": 200
                }
                """;

        mockMvc.perform(put("/tasks/{taskId}/progress", "T-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false));
    }
}
