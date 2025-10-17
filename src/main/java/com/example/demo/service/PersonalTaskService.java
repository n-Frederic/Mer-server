package com.example.demo.service;

import com.example.demo.entity.PersonalTask;
import com.example.demo.repository.PersonalTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonalTaskService {

    private final PersonalTaskRepository personalTaskRepository;

    public PersonalTaskService(PersonalTaskRepository personalTaskRepository) {
        this.personalTaskRepository = personalTaskRepository;
    }

    // 获取个人重要事项
    public PersonalTask getPersonalTask() {
        return personalTaskRepository.findAll().stream().findFirst().orElse(null);
    }

    // 更新个人重要事项
    public PersonalTask updatePersonalTask(List<String> tasks) {
        PersonalTask personalTask = getPersonalTask();
        if (personalTask == null) {
            personalTask = new PersonalTask(tasks);
        } else {
            personalTask.setTasks(tasks);
        }
        return personalTaskRepository.save(personalTask);
    }
}
