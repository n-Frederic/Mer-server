// PersonalTaskController.java
package com.example.demo.controller;

import com.example.demo.entity.PersonalTask;
import com.example.demo.service.PersonalTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/personal-task")
public class PersonalTaskController {

    private final PersonalTaskService personalTaskService;

    public PersonalTaskController(PersonalTaskService personalTaskService) {
        this.personalTaskService = personalTaskService;
    }

    // 根据用户ID获取个人任务
    @GetMapping("/user/{userId}")
    public Map<String, Object> getPersonalTask(@PathVariable Long userId) {
        try {
            PersonalTask personalTask = personalTaskService.getPersonalTaskByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("user_id", userId);
            response.put("personal_tasks", personalTask.getPersonalTasks());
            return response;
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return response;
        }
    }

    // 更新指定用户的个人任务
    @PutMapping("/user/{userId}")
    public Map<String, Object> updatePersonalTask(
            @PathVariable Long userId,
            @RequestBody Map<String, List<String>> requestBody) {

        Map<String, Object> response = new HashMap<>();
        List<String> personalTasks = requestBody.get("personal_tasks");

        if (personalTasks == null) {
            response.put("error", "Invalid data format");
            return response;
        }

        try {
            PersonalTask updatedPersonalTask = personalTaskService.updatePersonalTask(userId, personalTasks);
            response.put("message", "Personal task updated successfully");
            response.put("user_id", userId);
            response.put("updated_personal_tasks", updatedPersonalTask.getPersonalTasks());
            return response;
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return response;
        }
    }

    // 为指定用户创建个人任务
    @PostMapping("/user/{userId}")
    public Map<String, Object> createPersonalTask(
            @PathVariable Long userId,
            @RequestBody Map<String, List<String>> requestBody) {

        Map<String, Object> response = new HashMap<>();
        List<String> personalTasks = requestBody.get("personal_tasks");

        if (personalTasks == null) {
            response.put("error", "Invalid data format");
            return response;
        }

        try {
            PersonalTask createdPersonalTask = personalTaskService.createPersonalTask(userId, personalTasks);
            response.put("message", "Personal task created successfully");
            response.put("user_id", userId);
            response.put("created_personal_tasks", createdPersonalTask.getPersonalTasks());
            return response;
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return response;
        }
    }
}