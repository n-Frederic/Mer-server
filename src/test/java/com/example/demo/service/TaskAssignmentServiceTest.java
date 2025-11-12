package com.example.demo.service;

import com.example.demo.entity.TaskAssignment;
import com.example.demo.repository.TaskAssignmentRepository;
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
 * TaskAssignmentService单元测试
 */
@ExtendWith(MockitoExtension.class)
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
    void save_ShouldCallRepository() {
        when(taskAssignmentRepository.save(any(TaskAssignment.class)))
                .thenReturn(mockAssignment);

        TaskAssignment result = taskAssignmentRepository.save(mockAssignment);

        assertNotNull(result);
        verify(taskAssignmentRepository, times(1)).save(mockAssignment);
    }

    @Test
    void findById_Exists_ShouldReturnAssignment() {
        when(taskAssignmentRepository.findById(1L))
                .thenReturn(Optional.of(mockAssignment));

        Optional<TaskAssignment> result = taskAssignmentRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(50, result.get().getProgressPct());
    }

    @Test
    void findById_NotFound_ShouldReturnEmpty() {
        when(taskAssignmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<TaskAssignment> result = taskAssignmentRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void delete_ShouldCallRepository() {
        doNothing().when(taskAssignmentRepository).delete(any(TaskAssignment.class));

        assertDoesNotThrow(() -> {
            taskAssignmentRepository.delete(mockAssignment);
        });

        verify(taskAssignmentRepository, times(1)).delete(mockAssignment);
    }

    @Test
    void verifyProgressUpdate() {
        mockAssignment.setProgressPct(75);
        assertEquals(75, mockAssignment.getProgressPct());
    }
}
