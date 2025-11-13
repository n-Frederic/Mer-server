package com.example.demo.service;

import com.example.demo.entity.Log;
import com.example.demo.entity.User;
import com.example.demo.repository.LogRepository;
import com.example.demo.repository.LogTaskRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ✅ LogService 单元测试
 * 重点测试 deleteJournal() 方法的多种情况
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
        mockLog.setId(1L);
        mockLog.setAuthor(mockUser);
    }

    /**
     * ✅ 测试1：删除日志成功
     */
    @Test
    void deleteJournal_Success_ShouldDeleteLog() {
        when(logRepository.findById(1L)).thenReturn(Optional.of(mockLog));
        doNothing().when(logRepository).delete(any(Log.class));
        doNothing().when(logTaskRepository).deleteById_LogId(1L);

        assertDoesNotThrow(() -> logService.deleteJournal(1L));

        verify(logRepository, times(1)).findById(1L);
        verify(logTaskRepository, times(1)).deleteById_LogId(1L);
        verify(logRepository, times(1)).delete(mockLog);
    }

    /**
     * ✅ 测试2：日志不存在时应抛出 NoSuchElementException
     */
    @Test
    void deleteJournal_LogNotFound_ShouldThrowNoSuchElementException() {
        when(logRepository.findById(999L)).thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(NoSuchElementException.class, () -> logService.deleteJournal(999L));

        assertEquals("日志不存在", exception.getMessage());
        verify(logRepository, never()).delete(any());
        verify(logTaskRepository, never()).deleteById_LogId(anyLong());
    }

    /**
     * ✅ 测试3：验证 Repository 调用顺序
     */
    @Test
    void deleteJournal_ShouldCallRepositoriesInCorrectOrder() {
        when(logRepository.findById(1L)).thenReturn(Optional.of(mockLog));

        logService.deleteJournal(1L);

        var inOrder = inOrder(logRepository, logTaskRepository);
        inOrder.verify(logRepository).findById(1L);
        inOrder.verify(logTaskRepository).deleteById_LogId(1L);
        inOrder.verify(logRepository).delete(mockLog);
    }

    /**
     * ✅ 测试4：删除日志时触发 DataIntegrityViolationException（如外键冲突）
     */
    @Test
    void deleteJournal_DataIntegrityViolation_ShouldThrowSameException() {
        when(logRepository.findById(1L)).thenReturn(Optional.of(mockLog));
        doThrow(DataIntegrityViolationException.class).when(logRepository).delete(mockLog);

        assertThrows(DataIntegrityViolationException.class, () -> logService.deleteJournal(1L));

        verify(logTaskRepository, times(1)).deleteById_LogId(1L);
        verify(logRepository, times(1)).delete(mockLog);
    }

    /**
     * ✅ 测试5：确认 findById 确实被调用一次
     */
    @Test
    void deleteJournal_ShouldCallFindByIdOnce() {
        when(logRepository.findById(1L)).thenReturn(Optional.of(mockLog));

        logService.deleteJournal(1L);

        verify(logRepository, times(1)).findById(1L);
    }
}
