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
    public List<CompanyTask> replaceAllCompanyTasks(List<String> taskTitles) {
        // 1. 先删除所有旧的CompanyTask
        companyTaskRepository.deleteAll();

        // 2. 将前端传递的标题列表转换为CompanyTask对象列表
        List<CompanyTask> newTasks = taskTitles.stream()
                .map(title -> {
                    CompanyTask task = new CompanyTask();
                    task.setTitle(title);
                    // 补充其他默认字段（根据你的实体类需求设置，例如：

                    return task;
                })
                .collect(Collectors.toList());

        // 3. 批量插入新任务
        return companyTaskRepository.saveAll(newTasks);
    }


    /**
     * 删除任务
     */
    public void deleteCompanyTask(Long id) {
        companyTaskRepository.deleteById(id);
    }
}
