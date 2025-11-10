package com.example.demo.repository;
import com.example.demo.entity.Tags;
import com.example.demo.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagsRepository extends JpaRepository<Tags, Long>{
    List<Tags> findByTaskId(Long taskId);
}
