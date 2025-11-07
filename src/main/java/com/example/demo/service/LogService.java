package com.example.demo.service;

import com.example.demo.dto.LogRequestDTO;
import com.example.demo.dto.LogResponseDTO;
import com.example.demo.entity.Log;
import com.example.demo.entity.Log_Task;
import com.example.demo.entity.User;
import com.example.demo.repository.LogRepository;
import com.example.demo.repository.LogTaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final LogRepository logRepository;
    private final LogTaskRepository logTaskRepository;

    public LogService(LogRepository logRepository, LogTaskRepository logTaskRepository) {
        this.logRepository = logRepository;
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
}
