package com.example.demo.repository;

import com.example.demo.entity.CompanyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyTaskRepository extends JpaRepository<CompanyTask, Long> {

}