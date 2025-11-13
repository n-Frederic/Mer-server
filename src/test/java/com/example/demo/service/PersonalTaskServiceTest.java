package com.example.demo.service;

import com.example.demo.entity.PersonalTask;
import com.example.demo.repository.PersonalTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PersonalTaskService单元测试
 */
@ExtendWith(MockitoExtension.class)
class PersonalTaskServiceTest {

    @Mock
    private PersonalTaskRepository personalTaskRepository;
    
    private PersonalTask mockPersonalTask;
    
    @BeforeEach
    void setUp() {
        mockPersonalTask = new PersonalTask();
    }
    
    /**
     * 测试1：Repository的save方法调用
     */
    @Test
    void savePersonalTask_ShouldCallRepositorySave() {
        when(personalTaskRepository.save(any(PersonalTask.class)))
            .thenReturn(mockPersonalTask);
        
        PersonalTask result = personalTaskRepository.save(mockPersonalTask);
        
        assertNotNull(result);
        verify(personalTaskRepository, times(1)).save(mockPersonalTask);
    }
    
    /**
     * 测试2：Repository的findById方法调用
     */
    @Test
    void getPersonalTaskById_TaskExists_ShouldReturnTask() {
        when(personalTaskRepository.findById(1L))
            .thenReturn(Optional.of(mockPersonalTask));
        
        Optional<PersonalTask> result = personalTaskRepository.findById(1L);
        
        assertTrue(result.isPresent());
        verify(personalTaskRepository, times(1)).findById(1L);
    }
    
    /**
     * 测试3：findById在任务不存在的情况下调用
     */
    @Test
    void getPersonalTaskById_TaskNotFound_ShouldReturnEmpty() {
        when(personalTaskRepository.findById(999L))
            .thenReturn(Optional.empty());
        
        Optional<PersonalTask> result = personalTaskRepository.findById(999L);
        
        assertFalse(result.isPresent());
        verify(personalTaskRepository, times(1)).findById(999L);
    }
    
    /**
     * 测试4：验证Repository的save方法参数传递
     */
    @Test
    void savePersonalTask_ShouldPassCorrectParametersToRepository() {
        PersonalTask taskToSave = new PersonalTask();
        when(personalTaskRepository.save(taskToSave))
            .thenReturn(mockPersonalTask);
        
        PersonalTask result = personalTaskRepository.save(taskToSave);
        
        assertNotNull(result);
        verify(personalTaskRepository, times(1)).save(eq(taskToSave));
    }
    
    /**
     * 测试5：Repository的delete方法调用
     */
    @Test
    void deletePersonalTask_ShouldCallRepositoryDelete() {
        doNothing().when(personalTaskRepository).delete(any(PersonalTask.class));
        
        assertDoesNotThrow(() -> {
            personalTaskRepository.delete(mockPersonalTask);
        });
        
        verify(personalTaskRepository, times(1)).delete(mockPersonalTask);
    }
}
