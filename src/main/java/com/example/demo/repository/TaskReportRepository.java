package com.example.demo.repository;

import com.example.demo.entity.TaskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskReportRepository extends JpaRepository<TaskReport, Long> {
    List<TaskReport> findByTaskId(Long taskId);

    Optional<TaskReport> findByTaskIdAndReporterId(Long taskId, Long reporterId);

    @Query("SELECT COUNT(r) FROM TaskReport r WHERE r.taskId = :taskId AND r.status = 'approved'")
    long countApproved(Long taskId);

    @Query("SELECT COUNT(r) FROM TaskReport r WHERE r.taskId = :taskId AND r.status = 'submitted'")
    long countSubmitted(Long taskId);

    @Query("SELECT COUNT(r) FROM TaskReport r WHERE r.taskId = :taskId AND r.status = 'rejected'")
    long countRejected(Long taskId);
}
