// src/main/java/com/example/demo/service/FileService.java
package com.example.demo.service;

import com.example.demo.dto.FileUploadDTO;
import com.example.demo.entity.TaskReport;
import com.example.demo.repository.TaskReportRepository;
import com.example.demo.vo.FileUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final CosService cosService;
    private final TaskReportRepository taskReportRepository; // 添加Repository依赖

    @Value("${file.storage.type}")
    private String storageType;

    /**
     * 上传文件到COS
     */
    @Transactional
    public String uploadFile(MultipartFile file, Long taskId) {
        log.info("开始上传文件: taskId={}, originalFilename={}, size={} bytes",
                taskId, file.getOriginalFilename(), file.getSize());

        if (taskId == null) {
            taskId = 0L;
            log.warn("⚠️ taskId为null，使用默认值0");
        }

        // 生成唯一文件名
        String uuid = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = uuid + extension;

        // ✅ 统一使用 tasks/{taskId}/ 目录结构
        String cosKey = "tasks/" + taskId + "/" + fileName;

        log.info("上传到COS: key={}", cosKey);

        // 上传到COS
        String fileUrl = cosService.uploadFile(cosKey, file);

        boolean exists = cosService.doesObjectExist(cosKey);
        if (!exists) {
            log.error("❌ 文件上传后验证失败: {}", cosKey);
            throw new RuntimeException("文件上传失败，验证未通过");
        }

        log.info("✅ 文件上传完成: url={}, taskId={}, fileName={}", fileUrl, taskId, fileName);
        return fileUrl;
    }

    /**
     * 上传文件并关联到报告
     */
    @Transactional
    public String uploadFileForReport(MultipartFile file, Long taskId, Long reportId) {
        // 先上传文件
        String fileUrl = uploadFile(file, taskId);

        // 如果提供了reportId，更新报告附件列表
        if (reportId != null) {
            try {
                TaskReport report = taskReportRepository.findById(reportId)
                        .orElseThrow(() -> new RuntimeException("报告不存在"));

                // 使用实体类的辅助方法添加附件
                report.addAttachment(fileUrl);
                taskReportRepository.save(report);

                log.info("✅ 文件路径已保存到报告附件: reportId={}, url={}", reportId, fileUrl);
            } catch (Exception e) {
                log.error("保存附件路径到报告失败: reportId={}, url={}", reportId, fileUrl, e);
                // 可以选择不抛出异常，因为文件已成功上传到COS
            }
        }

        return fileUrl;
    }

    /**
     * 批量上传文件
     */
    @Transactional
    public List<String> uploadFilesForReport(List<MultipartFile> files, Long taskId, Long reportId) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String fileUrl = uploadFileForReport(file, taskId, reportId);
                fileUrls.add(fileUrl);
            } catch (Exception e) {
                log.error("批量上传中单个文件上传失败: {}", file.getOriginalFilename(), e);
                // 可以选择继续上传其他文件
                throw new RuntimeException("文件上传失败: " + file.getOriginalFilename(), e);
            }
        }

        return fileUrls;
    }

    public Resource loadFileAsResource(Long taskId, String filename) {
        log.info("加载文件资源: taskId={}, filename={}, storageType={}",
                taskId, filename, storageType);

        try {
            // 方案1: 首先尝试正式路径 tasks/{taskId}/
            String primaryKey = "tasks/" + taskId + "/" + filename;

            log.info("尝试从COS下载文件: {}", primaryKey);

            if (cosService.doesObjectExist(primaryKey)) {
                log.info("✅ 文件在正式路径找到: {}", primaryKey);
                InputStream inputStream = cosService.downloadFile(primaryKey);

                // 将InputStream转换为字节数组，避免重复读取问题
                byte[] fileBytes = inputStream.readAllBytes();
                inputStream.close();

                return new ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }

                    @Override
                    public long contentLength() {
                        return fileBytes.length;
                    }
                };
            }

            // 方案2: 如果在tasks/目录找不到，尝试test/目录（兼容旧数据）
            String testKey = "test/" + filename;
            log.info("正式路径未找到，尝试测试路径: {}", testKey);

            if (cosService.doesObjectExist(testKey)) {
                log.info("⚠️ 文件在测试路径找到: {}", testKey);
                InputStream inputStream = cosService.downloadFile(testKey);

                byte[] fileBytes = inputStream.readAllBytes();
                inputStream.close();

                return new ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }

                    @Override
                    public long contentLength() {
                        return fileBytes.length;
                    }
                };
            }

            // 如果所有路径都失败
            log.error("❌ 文件不存在于任何路径: taskId={}, filename={}", taskId, filename);
            log.error("查找过的路径:");
            log.error("  - 正式路径: {}", primaryKey);
            log.error("  - 测试路径: {}", testKey);
            throw new RuntimeException("文件不存在: " + filename);

        } catch (Exception e) {
            log.error("❌ 从COS加载文件失败: taskId={}, filename={}", taskId, filename, e);
            throw new RuntimeException("加载文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        try {
            cosService.deleteFileByUrl(fileUrl);
            log.info("文件删除成功: {}", fileUrl);
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileUrl, e);
            throw new RuntimeException("删除文件失败", e);
        }
    }

    /**
     * 从报告中删除附件
     */
    @Transactional
    public void deleteReportAttachment(Long reportId, String fileUrl) {
        try {
            TaskReport report = taskReportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("报告不存在"));

            // 从附件列表中移除
            List<String> attachments = report.getAttachmentList();
            if (attachments.remove(fileUrl)) {
                report.setAttachmentList(attachments);
                taskReportRepository.save(report);
                log.info("✅ 从数据库删除附件引用: reportId={}, url={}", reportId, fileUrl);

                // 可选：同时删除COS中的文件
                // deleteFile(fileUrl);
            } else {
                log.warn("附件不在列表中: reportId={}, url={}", reportId, fileUrl);
            }
        } catch (Exception e) {
            log.error("从报告中删除附件失败: reportId={}, url={}", reportId, fileUrl, e);
            throw new RuntimeException("删除附件失败", e);
        }
    }

    /**
     * 获取报告的附件列表
     */
    public List<String> getReportAttachments(Long reportId) {
        try {
            TaskReport report = taskReportRepository.findById(reportId)
                    .orElseThrow(() -> new RuntimeException("报告不存在"));
            return report.getAttachmentList();
        } catch (Exception e) {
            log.error("获取报告附件列表失败: reportId={}", reportId, e);
            return List.of();
        }
    }
}