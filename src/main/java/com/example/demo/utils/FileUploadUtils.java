package com.example.demo.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUploadUtils {

    private String uploadDir;

    @Value("${file.upload-dir:uploads}")
    public void setUploadDir(String dir) {
        // 转换为绝对路径
        this.uploadDir = new File(dir).getAbsolutePath();
        System.out.println("文件上传目录: " + this.uploadDir);
    }

    public String saveFile(MultipartFile file, String folder) throws IOException {
        // 确保 uploadDir 不为空
        if (uploadDir == null) {
            uploadDir = new File("uploads").getAbsolutePath();
        }

        String dirPath = uploadDir + File.separator + folder;

        System.out.println("目标目录: " + dirPath);

        // 使用 Path 确保目录创建
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            System.out.println("目录创建成功: " + dir.toAbsolutePath());
        }

        String ext = "";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            int idx = originalFilename.lastIndexOf(".");
            if (idx > 0) {
                ext = originalFilename.substring(idx);
            }
        }

        String filename = UUID.randomUUID() + ext;
        String fullPath = dirPath + File.separator + filename;

        System.out.println("完整文件路径: " + fullPath);

        // 使用 transferTo 保存文件
        File dest = new File(fullPath);
        try {
            file.transferTo(dest);
            System.out.println("文件保存成功: " + fullPath);
            System.out.println("文件是否存在: " + dest.exists());
            System.out.println("文件大小: " + dest.length());
        } catch (IOException e) {
            System.err.println("文件保存失败: " + e.getMessage());
            throw e;
        }

        // 返回相对路径（便于移动端访问）
        return folder + "/" + filename;
    }
}