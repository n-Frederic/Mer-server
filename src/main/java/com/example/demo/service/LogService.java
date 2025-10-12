package com.example.demo.service;

import com.example.demo.dto.LogRequestDTO;
import com.example.demo.dto.LogResponseDTO;
import com.example.demo.entity.Log;
import com.example.demo.entity.User;
import com.example.demo.repository.LogRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public Page<Log> getLogsByUser(Long authorId, String date, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        String dateFilter = (date == null || date.isEmpty()) ? "" : date;
        return logRepository.findByAuthor_IdAndDateContaining(authorId, dateFilter, pageable);
    }

    @Transactional
    public LogResponseDTO createLog(LogRequestDTO request, User author) {
        Log log = new Log(
                request.getTitle(),
                request.getDate(),
                request.getSummary(),
                request.getContent(),
                author
        );

        Log saved = logRepository.save(log);
        String customId = String.format("L-%03d", saved.getId());

        return new LogResponseDTO(true, customId);
    }
}
