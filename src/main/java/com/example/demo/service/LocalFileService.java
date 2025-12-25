// src/main/java/com/example/demo/service/LocalFileService.java
package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileService {

    private final Path fileStorageLocation;

    public LocalFileService() {
        this.fileStorageLocation = Paths.get("./uploads")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("本地文件存储目录: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("无法创建上传目录", ex);
        }
    }

    public String saveFile(MultipartFile file, Long taskId) {
        try {
            // 创建任务目录
            Path taskDir = this.fileStorageLocation
                    .resolve("task_reports")
                    .resolve(taskId != null ? taskId.toString() : "common");
            Files.createDirectories(taskDir);

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + extension;

            // 保存文件
            Path targetLocation = taskDir.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation);

            // 返回相对路径
            String relativePath = "task_reports/" + (taskId != null ? taskId : "common") + "/" + filename;
            log.info("文件保存到本地: {} -> {}", originalFilename, targetLocation);
            return relativePath;

        } catch (IOException ex) {
            log.error("保存文件失败", ex);
            throw new RuntimeException("保存文件失败: " + ex.getMessage(), ex);
        }
    }

    public Resource loadFileAsResource(Long taskId, String filename) {
        try {
            Path filePath = this.fileStorageLocation
                    .resolve("task_reports")
                    .resolve(taskId.toString())
                    .resolve(filename)
                    .normalize();

            log.info("加载文件: taskId={}, filename={}, path={}", taskId, filename, filePath);

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.error("文件不存在或不可读: {}", filePath);
                throw new RuntimeException("文件不存在: " + filename);
            }
        } catch (MalformedURLException ex) {
            log.error("文件路径格式错误", ex);
            throw new RuntimeException("文件路径格式错误: " + filename, ex);
        } catch (Exception ex) {
            log.error("加载文件失败", ex);
            throw new RuntimeException("加载文件失败: " + filename, ex);
        }
    }

    public boolean fileExists(Long taskId, String filename) {
        try {
            Path filePath = this.fileStorageLocation
                    .resolve("task_reports")
                    .resolve(taskId.toString())
                    .resolve(filename)
                    .normalize();
            return Files.exists(filePath) && Files.isReadable(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            // 如果是相对路径（本地存储的路径格式）
            Path filePath;
            if (fileUrl.startsWith("task_reports/")) {
                // 处理相对路径
                filePath = this.fileStorageLocation.resolve(fileUrl);
            } else if (fileUrl.contains("/uploads/task_reports/")) {
                // 处理绝对路径中的部分
                String relativePath = fileUrl.substring(fileUrl.indexOf("/uploads/") + 8);
                filePath = this.fileStorageLocation.resolve(relativePath);
            } else {
                // 尝试直接作为路径
                filePath = Paths.get(fileUrl);
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("已删除本地文件: {}", filePath);
            } else {
                log.warn("本地文件不存在: {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("删除本地文件失败: {}", fileUrl, e);
            // 不抛出异常，因为可能是COS文件
        }
    }


}