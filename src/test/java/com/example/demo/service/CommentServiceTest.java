package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommentService单元测试
 * 测试评论部分的核心功能
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private CommentService commentService;
    
    private User mockAuthor;
    private Comment mockComment;
    
    @BeforeEach
    void setUp() {
        mockAuthor = new User();
        mockAuthor.setId(1001L);
        mockAuthor.setName("测试用户");
        mockAuthor.setEmail("test@example.com");
        mockAuthor.setRole_id(4);
        mockComment = new Comment("task", 1L, mockAuthor, "测试评论1");
    }
    
    /**
     * 测试1：获取评论列表成功
     */
    @Test
    void getComments_Success_ShouldReturnCommentPage() {
        // 准备：模拟Repository返回评论分页数据
        Page<Comment> commentPage = new PageImpl<>(
            Arrays.asList(mockComment), 
            PageRequest.of(0, 10), 
            1
        );
        
        when(commentRepository.findByOwnerTypeAndOwnerId(
            eq("task"), eq(1L), any(PageRequest.class)
        )).thenReturn(commentPage);
        
        // 调用Service方法
        Page<Comment> result = commentService.getComments("task", "T-001", 1, 10);
        
        // 检查返回结果
        assertNotNull(result, "返回结果不应为null");
        assertEquals(1, result.getTotalElements(), "应返回1条评论");
        assertEquals("测试评论1", result.getContent().get(0).getContent());
        assertEquals("task", result.getContent().get(0).getOwnerType());
        
        // 验证：Repository方法被调用了1次
        verify(commentRepository, times(1))
            .findByOwnerTypeAndOwnerId(eq("task"), eq(1L), any(PageRequest.class));
    }
    
    /**
     * 测试2：获取评论列表但数据库内无评论
     */
    @Test
    void getComments_NoComments_ShouldReturnEmptyPage() {
        Page<Comment> emptyPage = new PageImpl<>(Arrays.asList());
        
        when(commentRepository.findByOwnerTypeAndOwnerId(
            anyString(), anyLong(), any(PageRequest.class)
        )).thenReturn(emptyPage);
        
        Page<Comment> result = commentService.getComments("task", "T-999", 1, 10);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
    
    /**
     * 测试3：作者本人删除评论(权限验证)
     */
    @Test
    void deleteComment_ByAuthor_ShouldDeleteSuccessfully() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(userRepository.findById(1001L)).thenReturn(Optional.of(mockAuthor));
        
        assertDoesNotThrow(() -> {
            commentService.deleteComment("C-001", "U-1001");
        });
        
        verify(commentRepository, times(1)).delete(mockComment);
    }
    
    /**
     * 测试4：非作者尝试删除评论
     */
    @Test
    void deleteComment_ByNonAuthor_ShouldThrowSecurityException() {
        User otherUser = new User();
        otherUser.setId(2002L);
        otherUser.setRole_id(4); // 普通成员
        
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(userRepository.findById(2002L)).thenReturn(Optional.of(otherUser));
        
        assertThrows(SecurityException.class, () -> {
            commentService.deleteComment("C-001", "U-2002");
        }, "非作者删除评论应抛出权限异常");
        
        verify(commentRepository, never()).delete(any());
    }
    
    /**
     * 测试5：管理员删除评论
     */
    @Test
    void deleteComment_ByAdmin_ShouldDeleteSuccessfully() {
        User admin = new User();
        admin.setId(5L);
        admin.setRole_id(5); //roleId=5 管理员
        
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(userRepository.findById(5L)).thenReturn(Optional.of(admin));
        
        assertDoesNotThrow(() -> {
            commentService.deleteComment("C-001", "U-5");
        });
        
        verify(commentRepository, times(1)).delete(mockComment);
    }
}
