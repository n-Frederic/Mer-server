package com.example.demo.controller;

import com.example.demo.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 获取个人任务
    @GetMapping("/personal")
    public Map<String, Object> getPersonalTasks(
            @RequestParam String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return taskService.getPersonalTasks(userId, status, priority, page, pageSize);
    }

    // 获取所有任务
    @GetMapping("/all")
    public Map<String, Object> getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return taskService.getAllTasks(status, priority, page, pageSize);
    }
}
