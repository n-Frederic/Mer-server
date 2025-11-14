
package com.example.demo.controller;

import com.example.demo.entity.Log;
import com.example.demo.entity.User;
import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.LogService;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogController.class)
@AutoConfigureMockMvc(addFilters = false)
@Feature("日志管理")
@Story("日志的增删查功能")
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogService logService;

    // 为了让 WebConfig / AuthInterceptor / LoginService 的依赖不报错
    @MockBean
    private LoginService loginService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        // 所有请求一律放行，避免被拦截掉
        Mockito.when(authInterceptor.preHandle(any(), any(), any()))
                .thenReturn(true);
    }

    // ========== GET /journals/scoped ==========

//    @Test
//    @DisplayName("GET /journals/scoped 返回分页日志列表")
//    void getScopedLogs_success() throws Exception {
        // 准备一个假的 Log + Page<Log>
//        Log log = Mockito.mock(Log.class);
//        Mockito.when(log.getId()).thenReturn(1L);
//        Mockito.when(log.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
//        Mockito.when(log.getCreatedAt()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
//        Mockito.when(log.getUpdatedAt()).thenReturn(Instant.parse("2025-01-01T10:00:00Z"));
//        Mockito.when(log.getSummary()).thenReturn("today");
//        Mockito.when(log.getTomorrowPlan()).thenReturn("tomorrow");
//        Mockito.when(log.getHelpNeeded()).thenReturn("help");
//        Mockito.when(log.getStatus()).thenReturn("NORMAL");
//
//        User author = Mockito.mock(User.class);
//        Mockito.when(author.getId()).thenReturn(1001L);
//        Mockito.when(log.getAuthor()).thenReturn(author);
//
//        Page<Log> page = new PageImpl<>(
//                List.of(log),
//                PageRequest.of(0, 10),
//                1
//        );
//
//        Mockito.when(logService.getScopedLogs(
//                        eq("my"),
//                        isNull(),
//                        eq("all"),
//                        isNull(),
//                        eq(1),
//                        eq(10)))
//                .thenReturn(page);
//
//        Mockito.when(logService.getTaskIdsByLogId(1L))
//                .thenReturn(List.of(11L, 22L));
//
//        Mockito.when(logService.getTagsForLog(1L))
//                .thenReturn(List.of("tag1", "tag2"));
//
//        mockMvc.perform(get("/journals/scoped")
//                        .param("mode", "my")
//                        .param("page", "1")
//                        .param("pageSize", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.list[0].log_id").value(1))
//                .andExpect(jsonPath("$.list[0].user_id").value(1001))
//                .andExpect(jsonPath("$.list[0].tags[0]").value("tag1"))
//                .andExpect(jsonPath("$.page").value(1))
//                .andExpect(jsonPath("$.pageSize").value(10))
//                .andExpect(jsonPath("$.total").value(1));
//    }

    @Test
    @DisplayName("GET /journals/scoped 返回默认分页日志列表")
    @Description("测试获取分页日志列表的功能，验证返回的日志数据、分页信息和关联的任务、标签")
    @Severity(SeverityLevel.CRITICAL)
    void getScopedLogs_success() throws Exception {
        // 准备一个假的 Log + Page<Log>
        Log log = Mockito.mock(Log.class);
        Mockito.when(log.getId()).thenReturn(1L);
        Mockito.when(log.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        Mockito.when(log.getCreatedAt()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
        Mockito.when(log.getUpdatedAt()).thenReturn(Instant.parse("2025-01-01T10:00:00Z"));
        Mockito.when(log.getSummary()).thenReturn("today");
        Mockito.when(log.getTomorrowPlan()).thenReturn("tomorrow");
        Mockito.when(log.getHelpNeeded()).thenReturn("help");
        Mockito.when(log.getStatus()).thenReturn("NORMAL");

        User author = Mockito.mock(User.class);
        Mockito.when(author.getId()).thenReturn(1001L);
        Mockito.when(log.getAuthor()).thenReturn(author);

        Page<Log> page = new PageImpl<>(
                List.of(log),
                PageRequest.of(0, 10), // 默认第一页，每页10条
                1
        );

        // 使用默认分页参数：page=1, pageSize=10
        Mockito.when(logService.getScopedLogs(
                        eq("my"),
                        isNull(),
                        eq("all"),
                        isNull(),
                        eq(1),  // 默认第一页
                        eq(10))) // 默认每页10条
                .thenReturn(page);

        Mockito.when(logService.getTaskIdsByLogId(1L))
                .thenReturn(List.of(11L, 22L));

        Mockito.when(logService.getTagsForLog(1L))
                .thenReturn(List.of("tag1", "tag2"));

        // 只传递mode参数，不传递page和pageSize
        mockMvc.perform(get("/journals/scoped")
                        .param("mode", "my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].log_id").value(1))  // 修正路径：$.data.list[0].log_id
                .andExpect(jsonPath("$.data.list[0].user_id").value(1001))
                .andExpect(jsonPath("$.data.list[0].todaySummary").value("today"))
                .andExpect(jsonPath("$.data.list[0].tomorrowPlan").value("tomorrow"))
                .andExpect(jsonPath("$.data.list[0].helpNeeded").value("help"))
                .andExpect(jsonPath("$.data.list[0].status").value("NORMAL"))
                .andExpect(jsonPath("$.data.list[0].tags[0]").value("tag1"))
                .andExpect(jsonPath("$.data.list[0].task_id[0]").value(11))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    // ========== DELETE /journals/{journalId} ==========

    @Test
    @DisplayName("DELETE /journals/{journalId} 日志删除成功")
    @Description("测试成功删除日志的功能，验证返回的成功消息")
    @Severity(SeverityLevel.CRITICAL)
    void deleteJournal_success() throws Exception {
        doNothing().when(logService).deleteJournal(1L);

        mockMvc.perform(delete("/journals/{journalId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("日志删除成功"));
    }

    @Test
    @DisplayName("DELETE /journals/{journalId} 外键约束导致无法删除，返回 400")
    @Description("测试因外键约束无法删除日志的场景，验证返回的约束违反错误")
    @Severity(SeverityLevel.CRITICAL)
    void deleteJournal_constraintViolation() throws Exception {
        doThrow(new DataIntegrityViolationException("fk"))
                .when(logService).deleteJournal(1L);

        mockMvc.perform(delete("/journals/{journalId}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.error").value("ConstraintViolation"));
    }

    @Test
    @DisplayName("DELETE /journals/{journalId} 日志不存在，返回 404")
    @Description("测试删除不存在的日志的场景，验证返回的404错误")
    @Severity(SeverityLevel.CRITICAL)
    void deleteJournal_notFound() throws Exception {
        doThrow(new NoSuchElementException("not found"))
                .when(logService).deleteJournal(1L);

        mockMvc.perform(delete("/journals/{journalId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("日志不存在"));
    }

    // ========== GET /journals/{logId} 详情 ==========

    @Test
    @DisplayName("GET /journals/{logId} 返回日志详情 Map")
    @Description("测试获取日志详情的功能，验证返回的日志详细信息")
    @Severity(SeverityLevel.CRITICAL)
    void getJournalDetail_success() throws Exception {
        Mockito.when(logService.getJournalDetail(1L))
                .thenReturn(
                        Map.of(
                                "log_id", 1L,
                                "todaySummary", "hello"
                        )
                );

        mockMvc.perform(get("/journals/{logId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log_id").value(1))
                .andExpect(jsonPath("$.todaySummary").value("hello"));
    }
}
