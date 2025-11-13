package com.example.demo.controller;

import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.LoginService;
import com.example.demo.service.TaskAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(TaskAssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)  // 一样关掉 Spring Security 过滤器
class TaskAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskAssignmentService taskAssignmentService;

    @MockBean
    private LoginService loginService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        // 拦截器一律放行
        Mockito.when(authInterceptor.preHandle(any(), any(), any()))
                .thenReturn(true);
    }

    @Test
    @DisplayName("TaskAssignmentController 能正常加载（目前尚无具体接口）")
    void contextLoads() {
        // 这里什么都不用写，只要测试类能跑通说明：
        // - Controller 构造注入没问题
        // - WebMvcTest 环境 + MockBean 依赖都配置正确
    }

    /*
     * 当你在 TaskAssignmentController 里新增具体接口，例如：
     *
     *   @PutMapping("/{taskId}/assignees")
     *   public ResponseEntity<?> updateAssignees(@PathVariable Long taskId,
     *                                            @RequestBody TaskAssigneesDTO dto) {...}
     *
     * 可以在这里仿照 TaskControllerTest / LogControllerTest 写具体的 MockMvc 测试：
     *
     *   - Mockito.when(taskAssignmentService.xxx(...)).thenReturn(...)
     *   - mockMvc.perform(put("/tasks/{taskId}/assignees", 1L)...).andExpect(...)
     *
     */
}
