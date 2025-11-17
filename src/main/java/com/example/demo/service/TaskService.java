package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.*;
import com.example.demo.utils.FileUploadUtils;
import com.example.demo.utils.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final TaskReportRepository taskReportRepository;
    private final TeamRepository teamRepository;
    private final RoleRepository roleRepository;
    private final NotificationRepository notificationRepository;
    private final LogRepository logRepository;
    private final FileUploadUtils fileUploadUtils;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, TaskAssignmentRepository taskAssignmentRepository, TagsRepository tagsRepository, TeamRepository teamRepository, RoleRepository roleRepository, TaskReportRepository taskReportRepository, LogRepository logRepository, FileUploadUtils fileUploadUtils) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
this.notificationRepository = notificationRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.tagsRepository=tagsRepository;
        this.taskReportRepository = taskReportRepository;
        this.teamRepository = teamRepository;
        this.roleRepository = roleRepository;
        this.logRepository = logRepository;
        this.fileUploadUtils = fileUploadUtils;
    }

    public Map<String, Object> getPersonalTasks(Long userId, String status, String priority, int page, int pageSize) {
        Page<Task> taskPage = taskRepository.findAssignedTasks(
                userId, PageRequest.of(page - 1, pageSize)
        );
        System.out.println(userId);
        System.out.println(taskPage.getContent());

        return wrapResponse2(taskPage, page, pageSize);
    }
    
    public Map<String, Object> getAssignee(
            Long currentUserId,
            String keyword,
            Long departmentId,
            Long teamId,
            int page,
            int pageSize) {

        // 查当前用户信息
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));

        Integer currentRole = currentUser.getRoleId();
        Integer targetRole = UserRole.getAssignableRoleId(currentRole);

        if (targetRole == null) {
            return ResponseUtils.wrapResponse(false, "该角色无权限派发任务");
        }

        // 分页参数
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 查询可派发用户
        Page<User> userPage = userRepository.searchAssignableUsers(
                targetRole,
                keyword,
                departmentId,
                teamId,
                pageable
        );

        // 结果封装
        Map<String, Object> data = Map.of(
                "list", userPage.getContent().stream()
                        .map(user -> new UserResponseDTO(
                                user.getId(),
                                user.getName(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getPhone(),
                                user.getStatus(),
                                user.getCreated_at(),
                                user.getLast_login(),
                                new UserResponseDTO.RoleDTO(user.getRoleId(), roleRepository.findByRoleId(user.getRoleId()).getName()),
                                new UserResponseDTO.TeamDTO(user.getTeamId(), teamRepository.findByTeamId(user.getTeamId()).getName())
                        ))
                        .toList(),

                "total", userPage.getTotalElements(),
                "page", page,
                "pageSize", pageSize,
                "totalPages", userPage.getTotalPages()
        );

        return ResponseUtils.wrapResponse(true, data);
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
                    "Published",           // status
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

                Notification notification = new Notification(task.getAssigneeIds().get(i),"task",savedTask.getId(),"A new task is assigned to you",savedTask.getTitle(),false);
                notificationRepository.save(notification);
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

        // ======== task 基础信息 ========
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

        // ======== progress_pct（如果 Task 里有 progress 字段） ========
        taskData.put("progress_pct", task.getProgress_pct());

        // ======== creator 嵌套对象 ========
        User creator = task.getCreator();
        Map<String, Object> creatorData = new HashMap<>();
        creatorData.put("userId", creator.getId());
        creatorData.put("name", creator.getName());
        creatorData.put("email", creator.getEmail());
        taskData.put("creator", creatorData);

        // ======== 查询 related_logs ========
        List<RelatedLogDTO> logs = logRepository.findLogsByTaskId(taskId);

        List<Map<String, Object>> relatedLogs = new ArrayList<>();
        for (RelatedLogDTO log : logs) {
            Map<String, Object> logData = new HashMap<>();
            logData.put("log_id", log.getLogId());
            logData.put("title", log.getTodaySummary());
            relatedLogs.add(logData);
        }

        taskData.put("related_logs", relatedLogs);

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

    public void updateTaskProgress(TaskProgressUpdateDTO request) {
        Long taskId = Long.parseLong(request.getTaskId().replace("T-", ""));

        Long pct = request.getProgressPct();
        if (pct < 0 || pct > 100) {
            throw new IllegalArgumentException("进度百分比必须在 0-100 范围内");
        }

        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("不存在该任务"));

        task.setProgress_pct(pct);
        taskRepository.save(task);
    }

    public Map<String, Object> getReports(Long taskId) {
        List<TaskReport> reports = taskReportRepository.findByTaskId(taskId);

        List<Map<String, Object>> reportList = reports.stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("report_id", r.getReportId());
            map.put("task_id", r.getTaskId());
            map.put("reporter_id", r.getReporterId());
            map.put("content", r.getContent());
            map.put("address", r.getAddress());
            map.put("attachments", r.getAttachments());
            map.put("created_at", r.getCreatedAt());
            return map;
        }).toList();

        return Map.of("ok", true, "reports", reportList);
    }

    //  创建任务报告（reporterId 从前端传或从 JWT 解析）

    public Map<String, Object> createReport(
            Long taskId,
            Long reporterId,
            String content,
            String address,
            List<MultipartFile> files
    ) {
        // 验证任务是否存在
        if (!taskRepository.existsById(taskId)) {
            return Map.of("ok", false, "message", "任务不存在");
        }

        // 验证用户是否存在
        if (!userRepository.existsById(reporterId)) {
            return Map.of("ok", false, "message", "用户不存在");
        }

        TaskReport report = new TaskReport();
        report.setTaskId(taskId);
        report.setReporterId(reporterId);
        report.setContent(content);
        report.setAddress(address);

        List<String> filePaths = new ArrayList<>();

        // 处理附件
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        System.out.println("处理文件: " + file.getOriginalFilename() + ", 大小: " + file.getSize());
                        String savedPath = fileUploadUtils.saveFile(file, "task_reports/" + taskId);
                        filePaths.add(savedPath);
                        System.out.println("文件保存路径: " + savedPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Map.of(
                                "ok", false,
                                "message", "文件保存失败: " + e.getMessage()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Map.of(
                                "ok", false,
                                "message", "文件处理异常: " + e.getMessage()
                        );
                    }
                }
            }
        }

        // 保存 JSON
        try {
            if (!filePaths.isEmpty()) {
                report.setAttachments(new ObjectMapper().writeValueAsString(filePaths));
                System.out.println("附件JSON: " + report.getAttachments());
            } else {
                report.setAttachments("[]");
            }
        } catch (Exception e) {
            return Map.of(
                    "ok", false,
                    "message", "JSON 转换失败: " + e.getMessage()
            );
        }

        report.setCreatedAt(LocalDateTime.now());
        taskReportRepository.save(report);


        Task task=taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("<UNK>"));
        Long ownerId = task.getCreator().getId();
        Notification notification = new Notification(ownerId,"task",task.getId(),"A new task report",report.getContent(),false);
        notificationRepository.save(notification);

        return Map.of("ok", true, "report", Map.of(
                "report_id", report.getReportId(),
                "task_id", taskId,
                "reporter_id", reporterId,
                "content", content,
                "address", address,
                "attachments", attachments,
                "created_at", report.getCreatedAt()
        ));

        System.out.println("任务报告创建成功，报告ID: " + report.getReportId());

        return Map.of(
                "ok", true,
                "message", "报告提交成功",
                "attachments", filePaths
        );

    }




    // 3. 更新任务状态
    public Map<String, Object> updateStatus(Long taskId, String status) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        taskRepository.save(task);

        return Map.of("ok", true);
    }

    @Transactional
    public Map<String, Object> getAssignerAndAssignee(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return Map.of(
                    "code", 404,
                    "message", "任务不存在或未找到相关人员信息",
                    "data", null
            );
        }

        List<TaskAssignment> list = taskAssignmentRepository.findByTaskId(taskId);
        List<Map<String, Object>> assignees = new ArrayList<>();
        Long assignedBy = list.get(0).getAssignedBy();
        Optional<User> as = userRepository.findById(assignedBy);
        User a = as.get();
        Map<String, Object> assigner = Map.of("user_id",a.getId(),"name",a.getName());

        for (TaskAssignment ta : list) {
            Long assignee_id = ta.getAssigneeId();
            Optional<User> users = userRepository.findById(assignee_id);
            User user = users.get();
            assignees.add(
                    Map.of(
                            "user_id", user.getId(),
                            "name", user.getName()
                    )
            );
        }

        if (assignees.isEmpty()) {
            return Map.of(
                    "code", 404,
                    "message", "任务不存在或未找到相关人员信息",
                    "data", null
            );
        }



        return Map.of(
                "code", 200,
                "message", "success",
                "data", Map.of(
                        "assigner", assigner,
                        "assignees", assignees
                )
        );
    }
}
