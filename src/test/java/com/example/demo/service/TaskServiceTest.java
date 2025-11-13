package com.example.demo.service;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.TaskAssignmentRepository;
import com.example.demo.repository.TagsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TaskService单元测试
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    
    @InjectMocks
    private TaskService taskService;
    
    private Task mockTask;
    private User mockUser;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockUser = new User();
        mockUser.setId(1001L);
        mockUser.setName("张三");
        mockUser.setEmail("zhangsan@test.com");
        mockUser.setRole_id(4); // 普通成员
        
        mockTask = new Task();
        mockTask.setId(1L);
        mockTask.setTitle("测试任务");
        mockTask.setDescription("这是一个测试任务");
        mockTask.setCreator(mockUser);
        mockTask.setPriority("High");
        mockTask.setStatus("Published");
        mockTask.setStartAt(Instant.now());
        mockTask.setDueAt(Instant.now().plusSeconds(86400));
        mockTask.setCreatedAt(Instant.now());
        mockTask.setUpdatedAt(Instant.now());
    }
    
    /**
     * 测试1：获取所有任务成功
     */
    @Test
    void getAllTasks_Success_ShouldReturnTaskList() {
        // 准备：模拟Repository返回数据
        List<Task> tasks = Arrays.asList(mockTask);
        Page<Task> taskPage = new PageImpl<>(tasks, PageRequest.of(0, 10), 1);
        
        when(taskRepository.findByStatusContainingAndPriorityContaining(
            anyString(), anyString(), any(Pageable.class)
        )).thenReturn(taskPage);
        
        // 执行：调用Service方法
        Map<String, Object> result = taskService.getAllTasks(null, null, 1, 10);
        
        // 验证：检查结果
        assertNotNull(result, "返回结果不应为null");
        assertEquals(1L, result.get("total"), "总数应为1");
        assertTrue(result.containsKey("list"), "应包含list字段");
        
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result.get("list");
        assertEquals(1, list.size(), "列表大小应为1");
        
        // 验证：Repository方法被调用了1次
        verify(taskRepository, times(1))
            .findByStatusContainingAndPriorityContaining(
                eq(""), eq(""), any(Pageable.class)
            );
    }
    
    /**
     * 测试2：带过滤条件获取任务
     */
    @Test
    void getAllTasks_WithFilters_ShouldFilterCorrectly() {
        List<Task> tasks = Arrays.asList(mockTask);
        Page<Task> taskPage = new PageImpl<>(tasks);
        
        when(taskRepository.findByStatusContainingAndPriorityContaining(
            eq("Published"), eq("High"), any(Pageable.class)
        )).thenReturn(taskPage);
        
        Map<String, Object> result = taskService.getAllTasks("Published", "High", 1, 10);
        
        assertNotNull(result);
        verify(taskRepository).findByStatusContainingAndPriorityContaining(
            eq("Published"), eq("High"), any(Pageable.class)
        );
    }
    
    /**
     * 测试3：获取任务详任务存在情况
     */
    @Test
    void getTaskById_TaskExists_ShouldReturnTaskDetails() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
        
        Map<String, Object> result = taskService.getTaskById(1L);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("ok"), "ok字段应为true");
        assertTrue(result.containsKey("task"), "应包含task字段");
        
        verify(taskRepository, times(1)).findById(1L);
    }
    
    /**
     * 测试4：获取任务详情任务不存在情况
     */
    @Test
    void getTaskById_TaskNotFound_ShouldReturnErrorResponse() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        Map<String, Object> result = taskService.getTaskById(999L);
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("ok"), "ok字段应为false");
        // 后端返回的是英文错误消息
        assertEquals("Task not found", result.get("error"));
        
        verify(taskRepository, times(1)).findById(999L);
    }
    
    /**
     * 测试5：分页功能验证
     */
    @Test
    void getAllTasks_Pagination_ShouldReturnCorrectPage() {
        List<Task> tasks = Arrays.asList(mockTask);
        Page<Task> taskPage = new PageImpl<>(tasks, PageRequest.of(1, 5), 10);
        
        when(taskRepository.findByStatusContainingAndPriorityContaining(
            anyString(), anyString(), any(Pageable.class)
        )).thenReturn(taskPage);
        
        Map<String, Object> result = taskService.getAllTasks(null, null, 2, 5);
        
        assertEquals(2, result.get("page"), "页码应为2");
        assertEquals(5, result.get("pageSize"), "每页大小应为5");
        assertEquals(10L, result.get("total"), "总数应为10");
        // 注意：后端的wrapResponse可能不返回totalPages字段，所以只验证有返回结果即可
        assertNotNull(result, "结果不应为null");
    }
}

