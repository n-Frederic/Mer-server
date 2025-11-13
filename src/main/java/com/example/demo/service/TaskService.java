package com.example.demo.service;

import com.example.demo.dto.TaskCreateDTO;
import com.example.demo.dto.TaskUpdateRequestDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TagsRepository tagsRepository;


    public TaskService(TaskRepository taskRepository, UserRepository userRepository, TaskAssignmentRepository taskAssignmentRepository, TagsRepository tagsRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;

        this.taskAssignmentRepository = taskAssignmentRepository;
        this.tagsRepository=tagsRepository;
    }

    public Map<String, Object> getPersonalTasks(Long userId, String status, String priority, int page, int pageSize) {
        Page<Task> taskPage = taskRepository.findAssignedTasks(
                userId, PageRequest.of(page - 1, pageSize)
        );
        System.out.println(userId);
        System.out.println(taskPage.getContent());

        return wrapResponse2(taskPage, page, pageSize);
    }
    public Map<String, Object> getAssignees(Long userId, String status, String priority, int page, int pageSize) {

        User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        Integer role=user.getRole_id();
        // 1. 构建分页参数（注意：JPA 页码从 0 开始，需将前端传入的 page 减 1）
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 2. 调用分页查询方法（假设需要查询 roleId > role 参数 且 !=5 的用户）

        Page<User> userPage = userRepository.findByRoleIdGreaterThanAndRoleIdNot(role, 5, pageable);


        return wrapResponse1(userPage, page, pageSize);
    }
    public Map<String, Object> getViewTasks(Long userId, String status, String priority, int page, int pageSize) {
        Page<Task> taskPage = taskRepository.findViewTasks(
                userId, PageRequest.of(page - 1, pageSize)
        );
        System.out.println(userId);
        System.out.println(taskPage.getContent());

        return wrapResponse2(taskPage, page, pageSize);
    }

    public Map<String, Object> getAllTasks(String status, String priority, int page, int pageSize) {
        Page<Task> taskPage = taskRepository.findByStatusContainingAndPriorityContaining(
                status == null ? "" : status,
                priority == null ? "" : priority,
                PageRequest.of(page - 1, pageSize)
        );
        return wrapResponse2(taskPage, page, pageSize);
    }

    public ResponseEntity<?> createTask(TaskCreateDTO task, Long userId) {
        try {
            // 1. 权限与合法性校验
            // 检查用户是否存在（getReferenceById 若不存在会抛 EntityNotFoundException）
            User creator = userRepository.getReferenceById(userId);

            // 检查必填字段（如 title 不能为空）
            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                // 失败响应：缺失必填字段
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("ok", false);
                errorResponse.put("error", "Missing required field: title");
                errorResponse.put("code", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 可添加其他校验（如 dueAt 不能早于当前时间等）
            if (task.getDueAt() != null && task.getDueAt().isBefore(Instant.now())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("ok", false);
                errorResponse.put("error", "Due time cannot be earlier than current time");
                errorResponse.put("code", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 2. 构造任务对象
            Instant now = Instant.now();
            Long pct= Long.valueOf(0);
            Task newTask = new Task(
                    now,                  // updateTime
                    now,                  // createTime
                    task.getDueAt(),      // dueAt
                    now,                  // startAt
                    "Reported",           // status
                     pct,                   //pct
                    task.getPriority(),   // priority
                    task.getDescription(),// description
                    task.getTitle(),      // title
                    creator               // creator
            );

            // 3. 保存任务
            Task savedTask = taskRepository.save(newTask);
            for (int i = 0; i < task.getAssigneeIds().size(); i++) {
                LocalDateTime assignedAt = LocalDateTime.now();
                TaskAssignment taskAssignment = new TaskAssignment(savedTask.getId(),task.getAssigneeIds().get(i),userId,assignedAt);

                taskAssignmentRepository.save(taskAssignment);
            }

            for(int i=0;i<task.getTags().size();i++){
                Tags tags=new Tags(savedTask, task.getTags().get(i));
                tagsRepository.save(tags);

            }

            // 4. 成功响应
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("ok", true);
            successResponse.put("taskId", savedTask.getId()); // 假设 Task 有 getId() 方法
            successResponse.put("message", "Task created successfully");
            return ResponseEntity.ok(successResponse);

        } catch (EntityNotFoundException e) {
            // 处理用户不存在的异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ok", false);
            errorResponse.put("error", "User not found with id: " + userId);
            errorResponse.put("code", "USER_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            // 处理其他未知异常
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ok", false);
            errorResponse.put("error", "Failed to create task: " + e.getMessage());
            errorResponse.put("code", "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    private Map<String, Object> wrapResponse(Page<Task> taskPage, int page, int pageSize) {
        Map<String, Object> response = new HashMap<>();
        response.put("list", taskPage.getContent());
        response.put("total", taskPage.getTotalElements());
        response.put("page", page);
        response.put("pageSize", pageSize);
        return response;
    }

    private Map<String, Object> wrapResponse1(Page<User> userPage, int page, int pageSize) {
        Map<String, Object> response = new HashMap<>();
        response.put("list", userPage.getContent());
        response.put("total", userPage.getTotalElements());
        response.put("page", page);
        response.put("pageSize", pageSize);
        return response;
    }
    private Map<String, Object> wrapResponse2(Page<Task> taskPage, int page, int pageSize) {
        Map<String, Object> response = new LinkedHashMap<>();  // 改用 LinkedHashMap

        // 转换任务列表
        List<Map<String, Object>> taskList = taskPage.getContent().stream()
                .map(task -> {
                    Map<String, Object> taskMap = new LinkedHashMap<>();  // 改用 LinkedHashMap
                    taskMap.put("taskId", task.getId());
                    taskMap.put("title", task.getTitle());
                    taskMap.put("description", task.getDescription());
                    taskMap.put("creatorId", task.getCreator().getId());
                    taskMap.put("priority", task.getPriority());
                    taskMap.put("status", task.getStatus());
                    taskMap.put("progress_pct",task.getProgress_pct());
                    taskMap.put("startAt", task.getStartAt());
                    taskMap.put("dueAt", task.getDueAt());
                    taskMap.put("createdAt", task.getCreatedAt());
                    taskMap.put("updatedAt", task.getUpdatedAt());
                    return taskMap;
                })
                .collect(Collectors.toList());

        response.put("list", taskList);
        response.put("total", taskPage.getTotalElements());
        response.put("page", page);
        response.put("pageSize", pageSize);
        return response;
    }

    public Map<String, Object> getTaskById(Long taskId) {
        Map<String, Object> result = new HashMap<>();
        Optional<Task> optionalTask = taskRepository.findById(taskId);

        if (optionalTask.isEmpty()) {
            result.put("ok", false);
            result.put("error", "Task not found");
            result.put("code", "TASK_NOT_FOUND");
            return result;
        }

        Task task = optionalTask.get();

        // 构造返回体
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("taskId", task.getId());
        taskData.put("title", task.getTitle());
        taskData.put("description", task.getDescription());
        taskData.put("creatorId", task.getCreator().getId());
        taskData.put("priority", task.getPriority());
        taskData.put("status", task.getStatus());
        taskData.put("startAt", task.getStartAt());
        taskData.put("dueAt", task.getDueAt());
        taskData.put("createdAt", task.getCreatedAt());
        taskData.put("updatedAt", task.getUpdatedAt());

        // 嵌套 creator 信息
        User creator = task.getCreator();
        Map<String, Object> creatorData = new HashMap<>();
        creatorData.put("userId", creator.getId());
        creatorData.put("name", creator.getName());
        creatorData.put("email", creator.getEmail());
        taskData.put("creator", creatorData);

        result.put("ok", true);
        result.put("task", taskData);

        return result;
    }

    @Transactional
    public Task updateTaskInfo(String taskIdStr, TaskUpdateRequestDTO dto) {

        Long taskId = Long.parseLong(taskIdStr.replace("T-", ""));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        // 1. 更新字段
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setStatus(dto.getStatus());
        task.setStartAt(dto.getStartAt());
        task.setDueAt(dto.getDueAt());
        task.setUpdatedAt(Instant.now());

        // 2. parentTask 处理
        if (dto.getParentTask() != null && !dto.getParentTask().isBlank()) {
            Long parentId = Long.parseLong(dto.getParentTask().replace("T-", ""));
            Task parent = taskRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("父任务不存在"));
            // 先假定你 task 表里有 parentTask 字段，需要在 Task 实体中手动加
            task.setParent_task(parent);
        }

        // 保存任务更新
        taskRepository.save(task);

        // 3. 删除旧标签
        tagsRepository.deleteByTask_Id(taskId);

        // 4. 插入新标签
        if (dto.getTags() != null) {
            for (String tag : dto.getTags()) {
                Tags newTag = new Tags(task, tag);
                tagsRepository.save(newTag);
            }
        }

        return task;
    }
}
