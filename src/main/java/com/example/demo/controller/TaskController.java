package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.TaskCreateDTO;
import com.example.demo.entity.User;
import com.example.demo.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskCreateDTO task) {
        Long userId=UserContext.getCurrentUserId();
        //分配任务
        return taskService.createTask(task,userId);
    }
    // 获取个人任务

    @GetMapping("/personal")
    public Map<String, Object> getPersonalTasks(
//            @RequestParam User creator,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = UserContext.getCurrentUserId();

//        return taskService.getPersonalTasks(creator.getId(), status, priority, page, pageSize);
        return taskService.getPersonalTasks(userId, status, priority, page, pageSize);
    }

    // 获取个人权限内任务
    @GetMapping("/myView")
    public Map<String, Object> getViewTasks(
//            @RequestParam User creator,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = UserContext.getCurrentUserId();

//        return taskService.getPersonalTasks(creator.getId(), status, priority, page, pageSize);
        return taskService.getViewTasks(userId, status, priority, page, pageSize);
    }

    //获取可分配员工
    @GetMapping("/assignees")
    public Map<String, Object> getAssignees(
//            @RequestParam User creator,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = UserContext.getCurrentUserId();

//        return taskService.getPersonalTasks(creator.getId(), status, priority, page, pageSize);
        return taskService.getAssignees(userId, status, priority, page, pageSize);
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

    // 查看任务详情
    @GetMapping("/{taskId}")
    public Map<String, Object> getTaskById(@PathVariable Long taskId) {
        return taskService.getTaskById(taskId);
    }



}
