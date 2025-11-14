package com.example.demo.controller;

import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.LoginService;
import com.example.demo.service.TeamService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@AutoConfigureMockMvc(addFilters = false)  // 关闭 Spring Security 过滤器（不过拦截器还是会生效）
@Feature("团队管理")
@Story("团队信息查询功能")
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService teamService;

    // 为了让 WebConfig / AuthInterceptor / LoginService 的依赖不报错
    @MockBean
    private LoginService loginService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        // 所有请求都放行，避免被拦截
        Mockito.when(authInterceptor.preHandle(any(), any(), any()))
                .thenReturn(true);
    }

    @Test
    @DisplayName("GET /team/teams 不带 department_id，返回全部团队列表")
    @Description("测试获取所有团队列表的功能，验证返回的团队数据")
    @Severity(SeverityLevel.CRITICAL)
    void getTeams_all() throws Exception {
        // 准备一个 service 返回值：List<Map<String, Object>>
        List<Map<String, Object>> list = List.of(
                Map.of(
                        "team_id", 1,
                        "name", "Team A",
                        "dept_id", 10
                ),
                Map.of(
                        "team_id", 2,
                        "name", "Team B",
                        "dept_id", 20
                )
        );

        Mockito.when(teamService.getTeams(null))
                .thenReturn(list);

        mockMvc.perform(get("/team/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.list[0].team_id").value(1))
                .andExpect(jsonPath("$.list[0].name").value("Team A"))
                .andExpect(jsonPath("$.list[0].dept_id").value(10))
                .andExpect(jsonPath("$.list[1].team_id").value(2));
    }

    @Test
    @DisplayName("GET /team/teams 带 department_id 参数，调用 service.getTeams(deptId)")
    @Description("测试根据部门ID获取团队列表的功能，验证筛选条件和返回数据")
    @Severity(SeverityLevel.CRITICAL)
    void getTeams_withDeptId() throws Exception {
        List<Map<String, Object>> list = List.of(
                Map.of(
                        "team_id", 3,
                        "name", "Team C",
                        "dept_id", 99
                )
        );

        Mockito.when(teamService.getTeams(99))
                .thenReturn(list);

        mockMvc.perform(get("/team/teams")
                        .param("department_id", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.list[0].team_id").value(3))
                .andExpect(jsonPath("$.list[0].dept_id").value(99));
    }
}
