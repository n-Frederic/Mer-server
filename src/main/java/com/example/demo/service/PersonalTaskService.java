// PersonalTaskService.java
package com.example.demo.service;

import com.example.demo.entity.PersonalTask;
import com.example.demo.repository.PersonalTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PersonalTaskService {

    private final PersonalTaskRepository personalTaskRepository;

    public PersonalTaskService(PersonalTaskRepository personalTaskRepository) {
        this.personalTaskRepository = personalTaskRepository;
    }

    // 根据用户ID获取个人任务
    public PersonalTask getPersonalTaskByUserId(Long userId) {
        return personalTaskRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("未找到用户ID为 " + userId + " 的个人任务"));
    }

    // 更新或创建个人任务
    public PersonalTask updatePersonalTask(Long userId, List<String> personalTasks) {
        // 首先尝试查找现有任务
        PersonalTask personalTask = personalTaskRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 如果不存在，创建新的
                    return new PersonalTask(userId, personalTasks);
                });

        // 更新任务列表
        personalTask.setPersonalTasks(personalTasks);
        return personalTaskRepository.save(personalTask);
    }

    // 为指定用户创建个人任务
    public PersonalTask createPersonalTask(Long userId, List<String> personalTasks) {
        // 检查是否已存在个人任务
        if (personalTaskRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("用户ID为 " + userId + " 的个人任务已存在");
        }

        PersonalTask personalTask = new PersonalTask(userId, personalTasks);
        return personalTaskRepository.save(personalTask);
    }

    // 获取或创建个人任务（如果不存在）
    public PersonalTask getOrCreatePersonalTask(Long userId, List<String> defaultTasks) {
        return personalTaskRepository.findByUserId(userId)
                .orElseGet(() -> {
                    PersonalTask newTask = new PersonalTask(userId, defaultTasks);
                    return personalTaskRepository.save(newTask);
                });
    }
}