package com.example.demo.service;

import com.example.demo.entity.Task;
import com.example.demo.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Map<String, Object> getPersonalTasks(String userId, String status, String priority, int page, int pageSize) {
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
}
