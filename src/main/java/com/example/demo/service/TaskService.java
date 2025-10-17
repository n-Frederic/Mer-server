package com.example.demo.service;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Map<String, Object> getPersonalTasks(Long userId, String status, String priority, int page, int pageSize) {
        Page<Task> taskPage = taskRepository.findByCreatorIdAndStatusContainingAndPriorityContaining(
                userId,
                status == null ? "" : status,
                priority == null ? "" : priority,
                PageRequest.of(page - 1, pageSize)
        );
        return wrapResponse(taskPage, page, pageSize);
    }

    public Map<String, Object> getAllTasks(String status, String priority, int page, int pageSize) {
        Page<Task> taskPage = taskRepository.findByStatusContainingAndPriorityContaining(
                status == null ? "" : status,
                priority == null ? "" : priority,
                PageRequest.of(page - 1, pageSize)
        );
        return wrapResponse(taskPage, page, pageSize);
    }

    private Map<String, Object> wrapResponse(Page<Task> taskPage, int page, int pageSize) {
        Map<String, Object> response = new HashMap<>();
        response.put("list", taskPage.getContent());
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
        taskData.put("taskId", task.getTaskId());
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
}
