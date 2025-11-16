package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.TaskCreateDTO;
import com.example.demo.dto.TaskProgressUpdateDTO;
import com.example.demo.dto.TaskUpdateRequestDTO;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public Map<String, Object> getAssignableUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "department_id") Long departmentId,
            @RequestParam(required = false, name = "team_id") Long teamId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Long userId = UserContext.getCurrentUserId();
        return taskService.getAssignees(userId, keyword, departmentId, teamId, page, pageSize);
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
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("taskId", "T-" + updated.getId());
            dataMap.put("title", updated.getTitle());
            dataMap.put("description", updated.getDescription());
            dataMap.put("priority", updated.getPriority());
            dataMap.put("status", updated.getStatus());
            dataMap.put("startAt", updated.getStartAt());
            dataMap.put("dueAt", updated.getDueAt()); // 允许 null
            dataMap.put("parentTask",
                    updated.getParent_task() != null
                            ? "T-" + updated.getParent_task().getId()
                            : null); // 允许 null
            dataMap.put("updatedAt", updated.getUpdatedAt()); // 允许 null

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("code", 200);
            responseMap.put("message", "任务更新成功");
            responseMap.put("data", dataMap);

            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            System.err.println("--- Task Update Exception START ---");
            e.printStackTrace();
            System.err.println("--- Task Update Exception END ---");

            return ResponseEntity.status(404).body(
                    Map.of(
                            "code", 404,
                            "message", "更新任务信息失败"
                    )
            );
        }
    }

    @PutMapping("/{taskId}/progress")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @RequestBody TaskProgressUpdateDTO request
    ) {

        try {
            taskService.updateTaskProgress(request);

            return ResponseEntity.ok(
                    Map.of(
                            "ok", true,
                            "message", "进度更新成功"
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "ok", false,
                            "message", e.getMessage()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "ok", false,
                            "message", "服务器内部错误"
                    )
            );
        }
    }

    @GetMapping("/{taskId}/reports")
    public Map<String, Object> getReports(@PathVariable Long taskId) {
        return taskService.getReports(taskId);
    }

    // 9.2 创建任务报告
    @PostMapping("/{taskId}/reports")
    public Map<String, Object> createReport(
            @PathVariable Long taskId,
            @RequestParam Long reporterId, // 或从 token 解析
            @RequestParam String content,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String attachments
    ) {
        return taskService.createReport(taskId, reporterId, content, address, attachments);
    }

    // 9.3 更新任务状态
    @PatchMapping("/{taskId}/status")
    public Map<String, Object> updateStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body
    ) {
        return taskService.updateStatus(taskId, body.get("status"));
    }

    @GetMapping("/assignerAndDesignee/{taskId}")
    public ResponseEntity<?> getAssignerAndDesignee(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getAssignees(taskId));
    }


}
