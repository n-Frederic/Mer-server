package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.TaskCreateDTO;
import com.example.demo.dto.TaskUpdateRequestDTO;
import com.example.demo.entity.Task;
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

    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable String taskId,
            @RequestBody TaskUpdateRequestDTO request
    ) {
        try {
            Task updated = taskService.updateTaskInfo(taskId, request);

            return ResponseEntity.ok(
                    Map.of(
                            "code", 200,
                            "message", "任务更新成功",
                            "data", Map.of(
                                    "taskId", "T-" + updated.getId(),
                                    "title", updated.getTitle(),
                                    "description", updated.getDescription(),
                                    "priority", updated.getPriority(),
                                    "status", updated.getStatus(),
                                    "startAt", updated.getStartAt(),
                                    "dueAt", updated.getDueAt(),
                                    "parentTask",
                                    updated.getParent_task() != null
                                            ? "T-" + updated.getParent_task().getId()
                                            : null,
                                    "updatedAt", updated.getUpdatedAt()
                            )
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "code", 404,
                            "message", "更新任务信息失败"
                    )
            );
        }
    }



}
