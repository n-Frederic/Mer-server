package com.example.demo.service;

import com.example.demo.entity.PersonalTask;
import com.example.demo.repository.PersonalTaskRepository;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PersonalTaskService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@Feature("个人任务服务")
@Story("个人任务业务逻辑处理")
class PersonalTaskServiceTest {

    @Mock
    private PersonalTaskRepository personalTaskRepository;

    @InjectMocks
    private PersonalTaskService personalTaskService;

    private PersonalTask mockPersonalTask;

    @BeforeEach
    void setUp() {
        mockPersonalTask = new PersonalTask();
        mockPersonalTask.setUserId(1001L);
        mockPersonalTask.setPersonalTasks(List.of("任务A", "任务B"));
    }

    /**
     * 测试1：根据用户ID获取个人任务（存在）
     */
    @Test
    @DisplayName("根据用户ID获取个人任务（存在）")
    @Description("测试获取存在的用户个人任务，验证返回的任务信息")
    @Severity(SeverityLevel.CRITICAL)
    void getPersonalTaskByUserId_ShouldReturnTask_WhenExists() {
        when(personalTaskRepository.findByUserId(1001L))
                .thenReturn(Optional.of(mockPersonalTask));

        PersonalTask result = personalTaskService.getPersonalTaskByUserId(1001L);

        assertNotNull(result);
        assertEquals(1001L, result.getUserId());
        assertEquals(2, result.getPersonalTasks().size());
        verify(personalTaskRepository, times(1)).findByUserId(1001L);
    }

    /**
     * 测试2：根据用户ID获取个人任务（不存在）
     */
    @Test
    @DisplayName("根据用户ID获取个人任务（不存在）")
    @Description("测试获取不存在的用户个人任务，验证抛出异常")
    @Severity(SeverityLevel.CRITICAL)
    void getPersonalTaskByUserId_ShouldThrowException_WhenNotFound() {
        when(personalTaskRepository.findByUserId(999L))
                .thenReturn(Optional.empty());

        Exception e = assertThrows(RuntimeException.class,
                () -> personalTaskService.getPersonalTaskByUserId(999L));

        assertTrue(e.getMessage().contains("未找到用户ID为 999"));
        verify(personalTaskRepository, times(1)).findByUserId(999L);
    }

    /**
     * 测试3：更新或创建任务（已有任务则更新）
     */
    @Test
    @DisplayName("更新或创建任务（已有任务则更新）")
    @Description("测试更新已存在的个人任务，验证任务更新成功")
    @Severity(SeverityLevel.CRITICAL)
    void updatePersonalTask_ShouldUpdateExisting_WhenTaskExists() {
        when(personalTaskRepository.findByUserId(1001L))
                .thenReturn(Optional.of(mockPersonalTask));
        when(personalTaskRepository.save(any())).thenReturn(mockPersonalTask);

        List<String> newTasks = List.of("新任务1", "新任务2");
        PersonalTask result = personalTaskService.updatePersonalTask(1001L, newTasks);

        assertEquals(newTasks, result.getPersonalTasks());
        verify(personalTaskRepository, times(1)).save(any(PersonalTask.class));
    }

    /**
     * 测试4：更新或创建任务（不存在则新建）
     */
    @Test
    @DisplayName("更新或创建任务（不存在则新建）")
    @Description("测试更新不存在的个人任务时创建新任务，验证任务创建成功")
    @Severity(SeverityLevel.CRITICAL)
    void updatePersonalTask_ShouldCreateNew_WhenTaskNotExists() {
        when(personalTaskRepository.findByUserId(1002L)).thenReturn(Optional.empty());
        when(personalTaskRepository.save(any())).thenReturn(mockPersonalTask);

        List<String> defaultTasks = List.of("任务X");
        PersonalTask result = personalTaskService.updatePersonalTask(1002L, defaultTasks);

        assertNotNull(result);
        verify(personalTaskRepository, times(1)).save(any(PersonalTask.class));
    }

    /**
     * 测试5：创建任务（正常创建）
     */
    @Test
    @DisplayName("创建任务（正常创建）")
    @Description("测试创建新的个人任务，验证任务保存成功")
    @Severity(SeverityLevel.CRITICAL)
    void createPersonalTask_ShouldSave_WhenNotExists() {
        when(personalTaskRepository.findByUserId(1003L)).thenReturn(Optional.empty());
        when(personalTaskRepository.save(any())).thenReturn(mockPersonalTask);

        List<String> tasks = List.of("任务1", "任务2");
        PersonalTask result = personalTaskService.createPersonalTask(1003L, tasks);

        assertNotNull(result);
        verify(personalTaskRepository, times(1)).save(any(PersonalTask.class));
    }

    /**
     * 测试6：创建任务（用户已有任务应抛异常）
     */
    @Test
    @DisplayName("创建任务（用户已有任务应抛异常）")
    @Description("测试为已有任务的用户创建任务，验证抛出异常")
    @Severity(SeverityLevel.CRITICAL)
    void createPersonalTask_ShouldThrowException_WhenAlreadyExists() {
        when(personalTaskRepository.findByUserId(1001L)).thenReturn(Optional.of(mockPersonalTask));

        assertThrows(RuntimeException.class, () ->
                personalTaskService.createPersonalTask(1001L, List.of("任务A")));
        verify(personalTaskRepository, never()).save(any());
    }

    /**
     * 测试7：getOrCreate（存在则直接返回）
     */
    @Test
    @DisplayName("getOrCreate（存在则直接返回）")
    @Description("测试获取或创建个人任务时任务已存在的情况，验证直接返回现有任务")
    @Severity(SeverityLevel.CRITICAL)
    void getOrCreatePersonalTask_ShouldReturnExisting_WhenExists() {
        when(personalTaskRepository.findByUserId(1001L)).thenReturn(Optional.of(mockPersonalTask));

        PersonalTask result = personalTaskService.getOrCreatePersonalTask(1001L, List.of("默认任务"));
        assertEquals(mockPersonalTask, result);
        verify(personalTaskRepository, never()).save(any());
    }

    /**
     * 测试8：getOrCreate（不存在则创建）
     */
    @Test
    @DisplayName("getOrCreate（不存在则创建）")
    @Description("测试获取或创建个人任务时任务不存在的情况，验证创建新任务")
    @Severity(SeverityLevel.CRITICAL)
    void getOrCreatePersonalTask_ShouldCreate_WhenNotExists() {
        when(personalTaskRepository.findByUserId(2001L)).thenReturn(Optional.empty());
        when(personalTaskRepository.save(any())).thenReturn(mockPersonalTask);

        PersonalTask result = personalTaskService.getOrCreatePersonalTask(2001L, List.of("默认任务"));

        assertNotNull(result);
        verify(personalTaskRepository, times(1)).save(any(PersonalTask.class));
    }
}
