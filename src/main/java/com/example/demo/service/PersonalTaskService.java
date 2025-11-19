// PersonalTaskService.java
package com.example.demo.service;

import com.example.demo.entity.PersonalTask;
import com.example.demo.repository.PersonalTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collections;

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
        return personalTaskRepository.findByUserId(userId)
                .map(existingTask -> {
                    existingTask.setPersonalTasks(personalTasks);
                    return personalTaskRepository.save(existingTask);
                })
                .orElseGet(() -> {
                    // 如果不存在，创建新的
                    PersonalTask newTask = new PersonalTask(userId, personalTasks);
                    return personalTaskRepository.save(newTask);
                });
    }

    // 为指定用户创建个人任务
    public PersonalTask createPersonalTask(Long userId, List<String> personalTasks) {
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
                    PersonalTask newTask = new PersonalTask(userId,
                            defaultTasks != null ? defaultTasks : Collections.emptyList());
                    return personalTaskRepository.save(newTask);
                });
    }
}