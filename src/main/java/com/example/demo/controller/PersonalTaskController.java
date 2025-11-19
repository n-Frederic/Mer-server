// PersonalTaskController.java
package com.example.demo.controller;

import com.example.demo.entity.PersonalTask;
import com.example.demo.service.PersonalTaskService;
import org.springframework.web.bind.annotation.*;
import com.example.demo.context.UserContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPersonalTask() {
        try {
            Long userId = UserContext.getCurrentUserId();
            System.out.println("获取个人任务，用户ID: " + userId);

            PersonalTask personalTask = personalTaskService.getPersonalTaskByUserId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("user_id", userId);
            response.put("personal_tasks", personalTask.getPersonalTasks());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("获取个人任务失败: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("code", HttpStatus.NOT_FOUND.value());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // 更新指定用户的个人任务
    @PutMapping
    public ResponseEntity<Map<String, Object>> updatePersonalTask(
            @RequestBody Map<String, List<String>> requestBody) {

        try {
            Long userId = UserContext.getCurrentUserId();
            List<String> personalTasks = requestBody.get("personal_tasks");

            if (personalTasks == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "请求数据格式无效，缺少 personal_tasks 字段");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            System.out.println("更新个人任务，用户ID: " + userId + ", 任务: " + personalTasks);

            PersonalTask updatedPersonalTask = personalTaskService.updatePersonalTask(userId, personalTasks);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "个人任务更新成功");
            response.put("user_id", userId);
            response.put("updated_personal_tasks", updatedPersonalTask.getPersonalTasks());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("更新个人任务失败: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 为指定用户创建个人任务
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPersonalTask(
            @RequestBody Map<String, List<String>> requestBody) {

        try {
            Long userId = UserContext.getCurrentUserId();
            List<String> personalTasks = requestBody.get("personal_tasks");

            if (personalTasks == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "请求数据格式无效，缺少 personal_tasks 字段");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            System.out.println("创建个人任务，用户ID: " + userId + ", 任务: " + personalTasks);

            PersonalTask createdPersonalTask = personalTaskService.createPersonalTask(userId, personalTasks);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "个人任务创建成功");
            response.put("user_id", userId);
            response.put("created_personal_tasks", createdPersonalTask.getPersonalTasks());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("创建个人任务失败: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }
}