package com.example.demo.controller;

import com.example.demo.entity.CompanyTask;
import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.CompanyTaskService;
import com.example.demo.service.LoginService;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyTaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Feature("公司任务管理")
@Story("公司任务的增删查改功能")
class CompanyTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyTaskService companyTaskService;

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
    @DisplayName("GET /company-tasks 返回全部公司任务和 total")
    @Description("测试获取所有公司任务列表的功能，验证返回的任务总数和列表数据")
    @Severity(SeverityLevel.CRITICAL)
    void getAllCompanyTasks_success() throws Exception {
        CompanyTask t1 = Mockito.mock(CompanyTask.class);
        CompanyTask t2 = Mockito.mock(CompanyTask.class);

        Mockito.when(companyTaskService.getAllCompanyTasks())
                .thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/company-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.tasks.length()").value(2));
    }

    @Test
    @DisplayName("GET /company-tasks/by-priority 根据优先级获取任务")
    @Description("测试根据优先级筛选公司任务的功能，验证返回的任务数量和优先级信息")
    @Severity(SeverityLevel.CRITICAL)
    void getTasksByPriority_success() throws Exception {
        Mockito.when(companyTaskService.getTasksByPriority("高"))
                .thenReturn(List.of("Task A", "Task B"));

        mockMvc.perform(get("/company-tasks/by-priority")
                        .param("priority", "高"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("高"))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.tasks[0]").value("Task A"));
    }

    @Test
    @DisplayName("GET /company-tasks/by-status 根据状态获取任务")
    @Description("测试根据状态筛选公司任务的功能，验证返回的任务数量和状态信息")
    @Severity(SeverityLevel.CRITICAL)
    void getTasksByStatus_success() throws Exception {
        CompanyTask t1 = Mockito.mock(CompanyTask.class);
        CompanyTask t2 = Mockito.mock(CompanyTask.class);

        Mockito.when(companyTaskService.getTasksByStatus("进行中"))
                .thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/company-tasks/by-status")
                        .param("status", "进行中"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("进行中"))
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("POST /company-tasks 创建公司任务，调用 saveCompanyTask")
    @Description("测试创建新的公司任务功能，验证任务创建成功并调用相应的服务方法")
    @Severity(SeverityLevel.CRITICAL)
    void createCompanyTask_success() throws Exception {
        CompanyTask saved = Mockito.mock(CompanyTask.class);

        Mockito.when(companyTaskService.saveCompanyTask(any(CompanyTask.class)))
                .thenReturn(saved);

        String body = """
                {
                  "title": "New Task",
                  "description": "Desc"
                }
                """;

        mockMvc.perform(post("/company-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        Mockito.verify(companyTaskService).saveCompanyTask(any(CompanyTask.class));
    }

    @Test
    @DisplayName("PUT /company-tasks/{id} 更新公司任务")
    @Description("测试更新现有公司任务的功能，验证任务更新成功并调用相应的服务方法")
    @Severity(SeverityLevel.CRITICAL)
    void updateCompanyTask_success() throws Exception {
        Long id = 1L;

        CompanyTask existing = Mockito.mock(CompanyTask.class);
        CompanyTask saved = Mockito.mock(CompanyTask.class);

        Mockito.when(companyTaskService.getCompanyTaskById(id))
                .thenReturn(existing);
        Mockito.when(companyTaskService.saveCompanyTask(existing))
                .thenReturn(saved);

        String body = """
                {
                  "title": "Updated Title",
                  "description": "Updated Desc",
                  "priority": "高",
                  "status": "进行中"
                }
                """;

        mockMvc.perform(put("/company-tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        Mockito.verify(companyTaskService).getCompanyTaskById(id);
        Mockito.verify(companyTaskService).saveCompanyTask(existing);
    }

    @Test
    @DisplayName("DELETE /company-tasks/{id} 删除公司任务")
    @Description("测试删除公司任务的功能，验证任务删除成功并调用相应的服务方法")
    @Severity(SeverityLevel.CRITICAL)
    void deleteCompanyTask_success() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/company-tasks/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(companyTaskService).deleteCompanyTask(id);
    }
}
