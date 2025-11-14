package com.example.demo.controller;

import com.example.demo.entity.Comment;
import com.example.demo.entity.User;
import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.service.CommentService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Feature("评论管理")
@Story("评论的增删查功能")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

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
    @DisplayName("POST /comments 创建评论成功，返回 201 和评论数据")
    @Description("测试用户成功创建评论的场景，验证返回状态码和评论数据")
    @Severity(SeverityLevel.CRITICAL)
    void createComment_success() throws Exception {
        Comment saved = Mockito.mock(Comment.class);
        User author = Mockito.mock(User.class);

        Mockito.when(saved.getAuthor()).thenReturn(author);
        Mockito.when(author.getId()).thenReturn(1001L);
        Mockito.when(author.getName()).thenReturn("Alice");
        Mockito.when(saved.getCommentId()).thenReturn(1L);
        Mockito.when(saved.getOwnerType()).thenReturn("TASK");
        Mockito.when(saved.getContent()).thenReturn("hello");
        Mockito.when(saved.getCreatedAt()).thenReturn(LocalDateTime.parse("2024-01-01T10:00:00"));

        Mockito.when(commentService.createComment(any()))
                .thenReturn(saved);

        String body = """
                {
                  "ownerType": "TASK",
                  "ownerId": "T-001",
                  "content": "hello"
                }
                """;

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.commentId").value("C-001"))
                .andExpect(jsonPath("$.data.ownerType").value("TASK"))
                .andExpect(jsonPath("$.data.authorInfo.userId").value("U-1001"));
    }

    @Test
    @DisplayName("GET /comments 获取评论列表")
    @Description("测试获取指定任务或资源的评论列表，验证分页和数据正确性")
    @Severity(SeverityLevel.CRITICAL)
    void getComments_success() throws Exception {
        Comment c1 = Mockito.mock(Comment.class);
        Comment c2 = Mockito.mock(Comment.class);
        User u1 = Mockito.mock(User.class);
        User u2 = Mockito.mock(User.class);

        Mockito.when(c1.getCommentId()).thenReturn(1L);
        Mockito.when(c2.getCommentId()).thenReturn(2L);

        Mockito.when(c1.getOwnerType()).thenReturn("TASK");
        Mockito.when(c2.getOwnerType()).thenReturn("TASK");

        Mockito.when(c1.getAuthor()).thenReturn(u1);
        Mockito.when(c2.getAuthor()).thenReturn(u2);

        Mockito.when(u1.getId()).thenReturn(1001L);
        Mockito.when(u2.getId()).thenReturn(1002L);
        Mockito.when(u1.getName()).thenReturn("Alice");
        Mockito.when(u2.getName()).thenReturn("Bob");

        Mockito.when(c1.getContent()).thenReturn("c1");
        Mockito.when(c2.getContent()).thenReturn("c2");

        Mockito.when(c1.getCreatedAt()).thenReturn(LocalDateTime.parse("2024-01-01T10:00:00"));
        Mockito.when(c2.getCreatedAt()).thenReturn(LocalDateTime.parse("2024-01-01T11:00:00"));

        Page<Comment> page = new PageImpl<>(List.of(c1, c2));

        Mockito.when(commentService.getComments(eq("TASK"), eq("T-001"), eq(1), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/comments")
                        .param("ownerType", "TASK")
                        .param("ownerId", "T-001")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].commentId").value("C-001"))
                .andExpect(jsonPath("$.list[1].commentId").value("C-002"))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    @DisplayName("DELETE /comments/{id} 删除成功")
    @Description("测试用户成功删除自己创建的评论")
    @Severity(SeverityLevel.CRITICAL)
    void deleteComment_success() throws Exception {
        mockMvc.perform(delete("/comments/C-001")
                        .param("userId", "U-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.commentId").value("C-001"));

        Mockito.verify(commentService).deleteComment("C-001", "U-1001");
    }

    @Test
    @DisplayName("DELETE /comments/{id} 无权限时返回 403")
    @Description("测试用户尝试删除他人评论时返回权限错误")
    @Severity(SeverityLevel.CRITICAL)
    void deleteComment_forbidden() throws Exception {
        Mockito.doThrow(new SecurityException("FORBIDDEN"))
                .when(commentService)
                .deleteComment("C-001", "U-1001");

        mockMvc.perform(delete("/comments/C-001")
                        .param("userId", "U-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("DELETE /comments/{id} 删除失败时返回 404")
    @Description("测试删除不存在的评论时返回404错误")
    @Severity(SeverityLevel.CRITICAL)
    void deleteComment_notFound() throws Exception {
        Mockito.doThrow(new RuntimeException("NOT_FOUND"))
                .when(commentService)
                .deleteComment("C-001", "U-1001");

        mockMvc.perform(delete("/comments/C-001")
                        .param("userId", "U-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}
