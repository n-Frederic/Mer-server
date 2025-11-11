package com.example.demo.repository;

import com.example.demo.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByCreatorIdAndStatusContainingAndPriorityContaining(
            Long creatorId, String status, String priority, Pageable pageable);

    Page<Task> findByStatusContainingAndPriorityContaining(
            String status, String priority, Pageable pageable);

    // 新增：按被分配者（assignee_id）查询其被指派的任务，支持 status/priority 模糊过滤与分页
    @Query(
            value = """
        select distinct t.*
        from task t
        join task_assignment ta on ta.task_id = t.task_id
        where ta.assignee_id = :userId
        """,
            countQuery = """
        select count(distinct t.task_id)
        from task t
        join task_assignment ta on ta.task_id = t.task_id
        where ta.assignee_id = :userId
        """,
            nativeQuery = true
    )
    Page<Task> findAssignedTasks(@Param("userId") Long userId, Pageable pageable);



    @Query(
            value = """
        select distinct t.*
        from task t
        join task_assignment ta on ta.task_id = t.task_id
        where ta.assignee_id = :userId or ta.assigned_by = :userId 
        """,
            countQuery = """
        select count(distinct t.task_id)
        from task t
        join task_assignment ta on ta.task_id = t.task_id
        where ta.assignee_id = :userId or ta.assigned_by = :userId 
        """,
            nativeQuery = true
    )
    Page<Task> findViewTasks(@Param("userId") Long userId, Pageable pageable);
}
