package com.example.demo.service;

import com.example.demo.entity.CompanyTask;
import com.example.demo.repository.CompanyTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyTaskService {

    private final CompanyTaskRepository companyTaskRepository;

    public CompanyTaskService(CompanyTaskRepository companyTaskRepository) {
        this.companyTaskRepository = companyTaskRepository;
    }

    /**
     * 获取所有公司任务
     */
    public List<CompanyTask> getAllCompanyTasks() {
        return companyTaskRepository.findAll();
    }

    /**
     * 获取公司重要任务标题列表
     */
    public List<String> getCompanyImportantTasks() {
        return companyTaskRepository.findAll().stream()
                .map(CompanyTask::getTitle)
                .collect(Collectors.toList());
    }

    /**
     * 根据优先级获取任务标题
     */
    public List<String> getTasksByPriority(String priority) {
        return companyTaskRepository.findByPriority(priority).stream()
                .map(CompanyTask::getTitle)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态获取任务
     */
    public List<CompanyTask> getTasksByStatus(String status) {
        return companyTaskRepository.findByStatus(status);
    }

    /**
     * 根据ID获取任务
     */
    public CompanyTask getCompanyTaskById(Long id) {
        return companyTaskRepository.findById(id).orElse(null);
    }

    /**
     * 保存或更新任务
     */
    public CompanyTask saveCompanyTask(CompanyTask companyTask) {
        return companyTaskRepository.save(companyTask);
    }

    /**
     * 删除任务
     */
    public void deleteCompanyTask(Long id) {
        companyTaskRepository.deleteById(id);
    }
}
