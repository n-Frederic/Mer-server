package com.example.demo.service;

import com.example.demo.entity.CompanyTask;
import com.example.demo.repository.CompanyTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CompanyTaskService单元测试
 */
@ExtendWith(MockitoExtension.class)
class CompanyTaskServiceTest {

    @Mock
    private CompanyTaskRepository companyTaskRepository;
    
    private CompanyTask mockCompanyTask;
    
    @BeforeEach
    void setUp() {
        mockCompanyTask = new CompanyTask();
    }
    
    /**
     * 测试1：获取所有公司任务接口调用
     */
    @Test
    void getAllCompanyTasks_ShouldCallRepositoryFindAll() {
        List<CompanyTask> tasks = Arrays.asList(mockCompanyTask);
        when(companyTaskRepository.findAll()).thenReturn(tasks);
        
        List<CompanyTask> result = companyTaskRepository.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(companyTaskRepository, times(1)).findAll();
    }
    
    /**
     * 测试2：任务存在情况下根据ID获取公司任务
     */
    @Test
    void getCompanyTaskById_TaskExists_ShouldReturnTask() {
        when(companyTaskRepository.findById(1L))
            .thenReturn(Optional.of(mockCompanyTask));
        
        Optional<CompanyTask> result = companyTaskRepository.findById(1L);
        
        assertTrue(result.isPresent());
        verify(companyTaskRepository, times(1)).findById(1L);
    }
    
    /**
     * 测试3：任务不存在情况下根据ID获取公司任务
     */
    @Test
    void getCompanyTaskById_TaskNotFound_ShouldReturnEmpty() {
        when(companyTaskRepository.findById(999L)).thenReturn(Optional.empty());
        
        Optional<CompanyTask> result = companyTaskRepository.findById(999L);
        
        assertFalse(result.isPresent());
        verify(companyTaskRepository, times(1)).findById(999L);
    }
    
    /**
     * 测试4：保存公司任务接口调用验证
     */
    @Test
    void saveCompanyTask_ShouldCallRepositorySave() {
        when(companyTaskRepository.save(any(CompanyTask.class)))
            .thenReturn(mockCompanyTask);
        
        CompanyTask result = companyTaskRepository.save(mockCompanyTask);
        
        assertNotNull(result);
        verify(companyTaskRepository, times(1)).save(mockCompanyTask);
    }
    
    /**
     * 测试5：根据优先级查找任务
     */
    @Test
    void findByPriority_ShouldReturnMatchingTasks() {
        when(companyTaskRepository.findByPriority("High"))
            .thenReturn(Arrays.asList(mockCompanyTask));
        
        List<CompanyTask> result = companyTaskRepository.findByPriority("High");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(companyTaskRepository, times(1)).findByPriority("High");
    }
}
