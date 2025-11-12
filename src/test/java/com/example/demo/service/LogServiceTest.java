package com.example.demo.service;

import com.example.demo.entity.Log;
import com.example.demo.entity.User;
import com.example.demo.repository.LogRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.LogTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LogService单元测试
 */
@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogRepository logRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private LogTaskRepository logTaskRepository;
    
    @InjectMocks
    private LogService logService;
    
    private User mockUser;
    private Log mockLog;
    
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1001L);
        mockUser.setName("张三");
        
        mockLog = new Log();
        mockLog.setAuthor(mockUser);
    }
    
    /**
     * 测试1：删除日志成功
     */
    @Test
    void deleteJournal_Success_ShouldDeleteLog() {
        when(logRepository.findById(1L)).thenReturn(Optional.of(mockLog));
        doNothing().when(logRepository).delete(any(Log.class));
        doNothing().when(logTaskRepository).deleteById_LogId(anyLong());
        
        assertDoesNotThrow(() -> {
            logService.deleteJournal(1L);
        });
        
        verify(logRepository, times(1)).delete(mockLog);
        verify(logTaskRepository, times(1)).deleteById_LogId(1L);
    }
    
    /**
     * 测试2：删除日志日志不存在
     */
    @Test
    void deleteJournal_LogNotFound_ShouldThrowException() {
        when(logRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(Exception.class, () -> {
            logService.deleteJournal(999L);
        });
        
        verify(logRepository, never()).delete(any());
    }
    
    /**
     * 测试3：验证Repository findById被调用
     */
    @Test
    void deleteJournal_ShouldCallRepositoryFindById() {
        when(logRepository.findById(1L)).thenReturn(Optional.of(mockLog));
        doNothing().when(logRepository).delete(any(Log.class));
        doNothing().when(logTaskRepository).deleteById_LogId(anyLong());
        
        logService.deleteJournal(1L);
        
        verify(logRepository, times(1)).findById(1L);
    }
    
    /**
     * 测试4：验证删除日志时同时删除关联数据
     */
    @Test
    void deleteJournal_ShouldAlsoDeleteLogTaskRelations() {
        when(logRepository.findById(1L)).thenReturn(Optional.of(mockLog));
        doNothing().when(logRepository).delete(any(Log.class));
        doNothing().when(logTaskRepository).deleteById_LogId(anyLong());
        
        logService.deleteJournal(1L);
        
        verify(logTaskRepository, times(1)).deleteById_LogId(1L);
    }
    
    /**
     * 测试5：验证Repository方法调用顺序
     */
    @Test
    void deleteJournal_ShouldCallRepositoriesInCorrectOrder() {
        when(logRepository.findById(1L)).thenReturn(Optional.of(mockLog));
        doNothing().when(logRepository).delete(any(Log.class));
        doNothing().when(logTaskRepository).deleteById_LogId(anyLong());
        
        logService.deleteJournal(1L);
        
        // 验证调用顺序
        var inOrder = inOrder(logRepository, logTaskRepository);
        inOrder.verify(logRepository).findById(1L);
        inOrder.verify(logTaskRepository).deleteById_LogId(1L);
        inOrder.verify(logRepository).delete(mockLog);
    }
}

