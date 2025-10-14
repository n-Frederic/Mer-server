package com.example.demo.controller;

import com.example.demo.entity.PersonalTask;
import com.example.demo.service.PersonalTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/personal-important-tasks") // 基础路径
public class PersonalTaskController {

    private final PersonalTaskService personalTaskService;

    public PersonalTaskController(PersonalTaskService personalTaskService) {
        this.personalTaskService = personalTaskService;
    }

    // GET 映射，去掉 "/" 直接用空字符串
    @GetMapping
    public Map<String, Object> getPersonalImportantTasks() {
        PersonalTask personalTask = personalTaskService.getPersonalTask();
        Map<String, Object> response = new HashMap<>();
        response.put("tasks", personalTask != null ? personalTask.getTasks() : List.of());
        return response;
    }

    // PUT 映射，同样去掉 "/"
    @PutMapping
    public Map<String, Object> updatePersonalImportantTasks(@RequestBody Map<String, List<String>> requestBody) {
        Map<String, Object> response = new HashMap<>();
        List<String> tasks = requestBody.get("tasks");

        if (tasks == null) {
            response.put("error", "Invalid data format");
            return response;
        }

        PersonalTask updatedTask = personalTaskService.updatePersonalTask(tasks);
        response.put("message", "Personal important tasks updated successfully");
        response.put("updated_tasks", updatedTask.getTasks());
        return response;
    }
}
