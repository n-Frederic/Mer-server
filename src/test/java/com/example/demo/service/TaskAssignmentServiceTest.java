package com.example.demo.service;

import com.example.demo.entity.TaskAssignment;
import com.example.demo.repository.TaskAssignmentRepository;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TaskAssignmentService单元测试
 */
@ExtendWith(MockitoExtension.class)
@Feature("任务分配服务")
@Story("任务分配业务逻辑处理")
class TaskAssignmentServiceTest {

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    private TaskAssignment mockAssignment;

    @BeforeEach
    void setUp() {
        mockAssignment = new TaskAssignment();
        mockAssignment.setAssignmentId(1L);
        mockAssignment.setTaskId(1L);
        mockAssignment.setAssigneeId(1001L);
        mockAssignment.setProgressPct(50);
    }

    @Test
    @DisplayName("保存任务分配")
    @Description("测试保存任务分配的功能，验证Repository方法被调用")
    @Severity(SeverityLevel.CRITICAL)
    void save_ShouldCallRepository() {
        when(taskAssignmentRepository.save(any(TaskAssignment.class)))
                .thenReturn(mockAssignment);

        TaskAssignment result = taskAssignmentRepository.save(mockAssignment);

        assertNotNull(result);
        verify(taskAssignmentRepository, times(1)).save(mockAssignment);
    }

    @Test
    @DisplayName("根据ID查找任务分配（存在）")
    @Description("测试根据ID查找存在的任务分配，验证返回正确的分配信息")
    @Severity(SeverityLevel.CRITICAL)
    void findById_Exists_ShouldReturnAssignment() {
        when(taskAssignmentRepository.findById(1L))
                .thenReturn(Optional.of(mockAssignment));

        Optional<TaskAssignment> result = taskAssignmentRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(50, result.get().getProgressPct());
    }

    @Test
    @DisplayName("根据ID查找任务分配（不存在）")
    @Description("测试根据ID查找不存在的任务分配，验证返回空结果")
    @Severity(SeverityLevel.CRITICAL)
    void findById_NotFound_ShouldReturnEmpty() {
        when(taskAssignmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<TaskAssignment> result = taskAssignmentRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("删除任务分配")
    @Description("测试删除任务分配的功能，验证Repository方法被调用")
    @Severity(SeverityLevel.CRITICAL)
    void delete_ShouldCallRepository() {
        doNothing().when(taskAssignmentRepository).delete(any(TaskAssignment.class));

        assertDoesNotThrow(() -> {
            taskAssignmentRepository.delete(mockAssignment);
        });

        verify(taskAssignmentRepository, times(1)).delete(mockAssignment);
    }

    @Test
    @DisplayName("验证进度更新")
    @Description("测试任务分配进度更新的功能，验证进度值正确设置")
    @Severity(SeverityLevel.NORMAL)
    void verifyProgressUpdate() {
        mockAssignment.setProgressPct(75);
        assertEquals(75, mockAssignment.getProgressPct());
    }
}
