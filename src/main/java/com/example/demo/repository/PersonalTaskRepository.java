package com.example.demo.repository;

import com.example.demo.entity.PersonalTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalTaskRepository extends JpaRepository<PersonalTask, Long> {
}
