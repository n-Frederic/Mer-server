package com.example.demo.repository;

import com.example.demo.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {

    // 按作者ID查询，并可选日期过滤
    Page<Log> findByAuthor_IdAndDateContaining(Long authorId, String date, Pageable pageable);
}
