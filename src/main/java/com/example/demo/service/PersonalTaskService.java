// PersonalTaskService.java
package com.example.demo.service;

import com.example.demo.entity.PersonalTask;
import com.example.demo.entity.User;
import com.example.demo.repository.PersonalTaskRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonalTaskService {

    private final PersonalTaskRepository personalTaskRepository;
    private final UserRepository userRepository;

    public PersonalTaskService(PersonalTaskRepository personalTaskRepository, UserRepository userRepository) {
        this.personalTaskRepository = personalTaskRepository;
        this.userRepository = userRepository;
    }

    // 根据用户ID获取个人任务
    public PersonalTask getPersonalTaskByUserId(Long userId) {
        return personalTaskRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Personal task not found for user id: " + userId));
    }

    // 更新指定用户的个人任务
    public PersonalTask updatePersonalTask(Long userId, List<String> personalTasks) {
        PersonalTask personalTask = personalTaskRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
                    return new PersonalTask(user, personalTasks);
                });

        personalTask.setPersonalTasks(personalTasks);
        return personalTaskRepository.save(personalTask);
    }

    // 为指定用户创建个人任务
    public PersonalTask createPersonalTask(Long userId, List<String> personalTasks) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 检查是否已存在
        if (personalTaskRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Personal task already exists for user id: " + userId);
        }

        PersonalTask personalTask = new PersonalTask(user, personalTasks);
        return personalTaskRepository.save(personalTask);
    }
}