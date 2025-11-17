package com.example.demo.repository;

import com.example.demo.dto.AssigneeDTO;
import com.example.demo.dto.StatusCount;
import com.example.demo.entity.Task;
import com.example.demo.entity.TaskAssignment;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {



    List<StatusCount> findWeeklyTaskStatusCount(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    List<TaskAssignment> findByTaskId(Long taskId);
    @Query(value = """
            SELECT 
                u.user_id AS userId,
                u.name    AS name
            FROM task_assignment ta
            JOIN user u ON ta.assignee_id = u.user_id
            WHERE ta.task_id = :taskId
            """, nativeQuery = true)
    List<AssigneeDTO> findAssigneesByTaskId(@Param("taskId") Long taskId);
}

