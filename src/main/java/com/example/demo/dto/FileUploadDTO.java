package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class FileUploadDTO {
    private Long taskId;
    private Long reportId;  // 新增，用于关联报告
    private MultipartFile file;
    private List<MultipartFile> files;  // 支持批量上传
}