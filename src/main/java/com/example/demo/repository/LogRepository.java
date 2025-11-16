package com.example.demo.repository;

import com.example.demo.dto.RelatedLogDTO;
import com.example.demo.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    // 按作者ID查询，并可选日期过滤
    Page<Log> findByAuthor_Id(Long authorId, Pageable pageable);

    // mode = "member", "approval"
    Page<Log> findByAuthorIdIn(List<Long> userIds, Pageable pageable);

    // 加时间过滤
    Page<Log> findByAuthorIdInAndDateBetween(
            List<Long> userIds,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    // 加关键词过滤
    Page<Log> findByAuthorIdInAndSummaryContainingIgnoreCase(
            List<Long> userIds,
            String keyword,
            Pageable pageable
    );

    // 全条件（时间 + 关键词）
    Page<Log> findByAuthorIdInAndDateBetweenAndSummaryContainingIgnoreCase(
            List<Long> userIds,
            LocalDate start,
            LocalDate end,
            String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT l.id AS logId,
               l.summary AS todaySummary
        FROM Log l
        JOIN Log_Task m ON l.id = m.task.id
        WHERE m.task.id = :taskId
    """)
    List<RelatedLogDTO> findLogsByTaskId(@Param("taskId") Long taskId);

    @Query("""
        SELECT l
        FROM Log l
        WHERE l.author.id = :userId
          AND l.createdAt >= :startOfWeek
    """)
    List<Log> findLogsForCurrentWeek(
            @Param("userId") Long userId,
            @Param("startOfWeek") LocalDateTime startOfWeek
    );
}
