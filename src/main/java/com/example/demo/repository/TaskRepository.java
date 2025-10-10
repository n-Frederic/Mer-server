package com.example.demo.repository;

import com.example.demo.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByCreatorIdAndStatusContainingAndPriorityContaining(
            String creatorId, String status, String priority, Pageable pageable);

    Page<Task> findByStatusContainingAndPriorityContaining(
            String status, String priority, Pageable pageable);
}
