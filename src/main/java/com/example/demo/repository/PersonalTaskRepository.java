// PersonalTaskRepository.java
package com.example.demo.repository;

import com.example.demo.entity.PersonalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalTaskRepository extends JpaRepository<PersonalTask, Long> {

    // 根据用户ID查找个人任务
    Optional<PersonalTask> findByUserId(Long userId);
}