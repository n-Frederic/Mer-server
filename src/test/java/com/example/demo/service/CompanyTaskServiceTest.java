package com.example.demo.service;

import com.example.demo.entity.CompanyTask;
import com.example.demo.repository.CompanyTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CompanyTaskService 单元测试
 * 测试公司任务相关的核心逻辑
 */
@ExtendWith(MockitoExtension.class)
class CompanyTaskServiceTest {

    @Mock
    private CompanyTaskRepository companyTaskRepository;

    @InjectMocks
    private CompanyTaskService companyTaskService;

    private CompanyTask mockTask1;
    private CompanyTask mockTask2;

    @BeforeEach
    void setUp() {
        mockTask1 = new CompanyTask();
        mockTask1.setTaskId(1L);
        mockTask1.setTitle("任务A");
        mockTask1.setPriority("High");
        mockTask1.setStatus("进行中");

        mockTask2 = new CompanyTask();
        mockTask2.setTaskId(2L);
        mockTask2.setTitle("任务B");
        mockTask2.setPriority("Low");
        mockTask2.setStatus("已完成");
    }

    /**
     * 测试1：获取所有公司任务
     */
    @Test
    void getAllCompanyTasks_ShouldReturnAllTasks() {
        when(companyTaskRepository.findAll()).thenReturn(Arrays.asList(mockTask1, mockTask2));

        List<CompanyTask> result = companyTaskService.getAllCompanyTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(companyTaskRepository, times(1)).findAll();
    }

    /**
     * 测试2：获取公司重要任务标题列表
     */
    @Test
    void getCompanyImportantTasks_ShouldReturnTitlesList() {
        when(companyTaskRepository.findAll()).thenReturn(Arrays.asList(mockTask1, mockTask2));

        List<String> titles = companyTaskService.getCompanyImportantTasks();

        assertNotNull(titles);
        assertEquals(Arrays.asList("任务A", "任务B"), titles);
        verify(companyTaskRepository, times(1)).findAll();
    }

    /**
     * 测试3：根据优先级获取任务标题
     */
    @Test
    void getTasksByPriority_ShouldReturnFilteredTitles() {
        when(companyTaskRepository.findByPriority("High"))
                .thenReturn(Collections.singletonList(mockTask1));

        List<String> result = companyTaskService.getTasksByPriority("High");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("任务A", result.get(0));
        verify(companyTaskRepository, times(1)).findByPriority("High");
    }

    /**
     * 测试4：根据状态获取任务
     */
    @Test
    void getTasksByStatus_ShouldReturnMatchingTasks() {
        when(companyTaskRepository.findByStatus("进行中"))
                .thenReturn(Collections.singletonList(mockTask1));

        List<CompanyTask> result = companyTaskService.getTasksByStatus("进行中");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("任务A", result.get(0).getTitle());
        verify(companyTaskRepository, times(1)).findByStatus("进行中");
    }

    /**
     * 测试5：根据ID获取任务（存在）
     */
    @Test
    void getCompanyTaskById_TaskExists_ShouldReturnTask() {
        when(companyTaskRepository.findById(1L)).thenReturn(Optional.of(mockTask1));

        CompanyTask result = companyTaskService.getCompanyTaskById(1L);

        assertNotNull(result);
        assertEquals("任务A", result.getTitle());
        verify(companyTaskRepository, times(1)).findById(1L);
    }

    /**
     * 测试6：根据ID获取任务（不存在）
     */
    @Test
    void getCompanyTaskById_TaskNotFound_ShouldReturnNull() {
        when(companyTaskRepository.findById(99L)).thenReturn(Optional.empty());

        CompanyTask result = companyTaskService.getCompanyTaskById(99L);

        assertNull(result);
        verify(companyTaskRepository, times(1)).findById(99L);
    }

    /**
     * 测试7：保存或更新任务
     */
    @Test
    void saveCompanyTask_ShouldSaveAndReturnTask() {
        when(companyTaskRepository.save(any(CompanyTask.class))).thenReturn(mockTask1);

        CompanyTask result = companyTaskService.saveCompanyTask(mockTask1);

        assertNotNull(result);
        assertEquals("任务A", result.getTitle());
        verify(companyTaskRepository, times(1)).save(mockTask1);
    }

    /**
     * 测试8：删除任务
     */
    @Test
    void deleteCompanyTask_ShouldCallDeleteById() {
        doNothing().when(companyTaskRepository).deleteById(1L);

        companyTaskService.deleteCompanyTask(1L);

        verify(companyTaskRepository, times(1)).deleteById(1L);
    }
}
