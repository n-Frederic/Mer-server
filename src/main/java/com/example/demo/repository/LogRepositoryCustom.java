package com.example.demo.repository;

import com.example.demo.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface LogRepositoryCustom {

    Page<Log> searchLogs(
            List<Long> userIds,
            LocalDate start,
            LocalDate end,
            String keyword,
            List<String> tags,
            Pageable pageable
    );
}

