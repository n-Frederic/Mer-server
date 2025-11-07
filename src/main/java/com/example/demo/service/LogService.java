package com.example.demo.service;

import com.example.demo.dto.LogRequestDTO;
import com.example.demo.dto.LogResponseDTO;
import com.example.demo.entity.Log;
import com.example.demo.entity.Log_Task;
import com.example.demo.entity.User;
import com.example.demo.repository.LogRepository;
import com.example.demo.repository.LogTaskRepository;
import com.example.demo.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogService {

    private final LogRepository logRepository;
    private final TaskRepository taskRepository;
    private final LogTaskRepository logTaskRepository;

    public LogService(LogRepository logRepository, TaskRepository taskRepository, LogTaskRepository logTaskRepository) {
        this.logRepository = logRepository;
        this.taskRepository = taskRepository;
        this.logTaskRepository = logTaskRepository;
    }

    public Page<Log> getLogsByUser(Long authorId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));


        return logRepository.findByAuthor_Id(authorId, pageable);
    }

    @Transactional
    public LogResponseDTO createLog(LogRequestDTO request, User author) {
        Log log = new Log(
                request.getLogDate(),
                request.getSummary(),
                request.getTomorrowPlan(),
                request.getHelpNeeded(),
                author
        );

        Log saved = logRepository.save(log);
        Long logId = saved.getId();

        if (request.getTaskId() != null && !request.getTaskId().isEmpty()) {

            for (Long taskId : request.getTaskId()) {

                Log_Task relation = new Log_Task(logId, taskId);

                logTaskRepository.save(relation);
            }
        }

        String customId = String.format("L-%03d", saved.getId());

        return new LogResponseDTO(true, customId);
    }

    public List<Map<String, Object>> getTasksByLogId(Long logId) {

        List<Log_Task> mappingList = logTaskRepository.findById_LogId(logId);

        List<Map<String, Object>> tasks = new ArrayList<>();

        for (Log_Task lt : mappingList) {
            Long taskId = lt.getId().getTaskId();

            taskRepository.findById(taskId).ifPresent(task -> {
                Map<String, Object> m = new HashMap<>();
                m.put("task_id", task.getTask_id());
                m.put("title", task.getTitle());
                tasks.add(m);
            });
        }

        return tasks;
    }
}
