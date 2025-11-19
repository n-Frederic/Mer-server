// FileService.java
package com.example.demo.service;

import com.example.demo.entity.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    private final Path fileStorageLocation;

    @Autowired
    private LoginService loginService;

    @Autowired
    public FileService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        System.out.println("🚀 FileService 初始化开始...");
        System.out.println("📁 配置的上传目录: " + uploadDir);

        // 尝试多种路径解析方式
        Path path = Paths.get(uploadDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir"))
                    .resolve(uploadDir)
                    .normalize();
        }

        this.fileStorageLocation = path;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            throw new RuntimeException("无法创建文件存储目录", e);
        }

        System.out.println("📁 最终存储路径: " + this.fileStorageLocation);
        System.out.println("📁 当前工作目录 (user.dir): " + System.getProperty("user.dir"));
        System.out.println("✅ FileService 初始化完成");

    }

    public Resource loadFileAsResource(Long taskId, String fileName) {
        try {
            System.out.println("=".repeat(80));
            System.out.println("🔍 FileService.loadFileAsResource 被调用:");
            System.out.println("   Task ID: " + taskId);
            System.out.println("   文件名: " + fileName);
            System.out.println("📁 存储根目录: " + this.fileStorageLocation);

            // 构建文件路径
            String relativePath = "task_reports/" + taskId + "/" + fileName;
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();

            System.out.println("📁 相对路径: " + relativePath);
            System.out.println("📁 完整文件路径: " + filePath.toString());
            System.out.println("📁 文件是否存在: " + Files.exists(filePath));

            // 如果文件不存在，提供详细的调试信息
            if (!Files.exists(filePath)) {
                System.out.println("❌ 文件不存在，开始详细路径检查...");

                // 检查所有可能的路径
                List<Path> possiblePaths = new ArrayList<>();

                // 基于当前存储目录的路径
                possiblePaths.add(filePath);

                // 基于工作目录的路径
                Path workDir = Paths.get("").toAbsolutePath();
                possiblePaths.add(workDir.resolve("uploads/task_reports/" + taskId + "/" + fileName));
                possiblePaths.add(workDir.resolve("./uploads/task_reports/" + taskId + "/" + fileName));
                possiblePaths.add(workDir.resolve("../uploads/task_reports/" + taskId + "/" + fileName));

                // 绝对路径
                possiblePaths.add(Paths.get("D:/project/Mer-server/uploads/task_reports/" + taskId + "/" + fileName));

                System.out.println("🔍 检查所有可能路径:");
                for (Path path : possiblePaths) {
                    System.out.println("   - " + path + " (存在: " + Files.exists(path) + ")");
                    if (Files.exists(path)) {
                        System.out.println("   ✅ 文件实际位置: " + path);
                        filePath = path;
                        break;
                    }
                }

                // 如果仍然没找到，抛出详细错误
                if (!Files.exists(filePath)) {
                    System.out.println("❌ 文件在所有可能路径中都不存在");
                    System.out.println("📁 存储根目录结构:");
                    printDirectoryStructure(this.fileStorageLocation, 1);
                    throw new RuntimeException("文件不存在: " + fileName + " at " + filePath);
                }
            }

            // 检查文件权限
            if (!Files.isReadable(filePath)) {
                System.out.println("❌ 文件不可读: " + filePath);
                throw new RuntimeException("文件不可读: " + fileName);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                System.out.println("✅ 文件成功加载: " + fileName);
                System.out.println("✅ 文件大小: " + Files.size(filePath) + " bytes");
                System.out.println("✅ 文件URL: " + resource.getURL());
                System.out.println("=".repeat(80));
                return resource;
            } else {
                System.out.println("❌ 资源不存在: " + fileName);
                throw new RuntimeException("文件不存在: " + fileName);
            }
        } catch (Exception ex) {
            System.out.println("❌ 文件加载失败: " + ex.getMessage());
            System.out.println("=".repeat(80));
            throw new RuntimeException("文件加载失败: " + fileName, ex);
        }
    }

    // 递归打印目录结构
    private void printDirectoryStructure(Path dir, int depth) {
        if (depth > 3) return; // 限制递归深度

        try {
            if (Files.exists(dir) && Files.isDirectory(dir)) {
                Files.list(dir).forEach(path -> {
                    try {
                        String indent = "  ".repeat(depth);
                        String type = Files.isDirectory(path) ? "[目录]" : "[文件]";
                        String size = "";
                        if (!Files.isDirectory(path)) {
                            try {
                                size = " (" + Files.size(path) + " bytes)";
                            } catch (Exception e) {
                                size = " (大小未知)";
                            }
                        }
                        System.out.println(indent + "📁 " + path.getFileName() + type + size);

                        if (Files.isDirectory(path) && depth < 3) {
                            printDirectoryStructure(path, depth + 1);
                        }
                    } catch (Exception e) {
                        String indent = "  ".repeat(depth);
                        System.out.println(indent + "❌ 无法访问: " + path.getFileName());
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("❌ 无法列出目录: " + dir);
        }
    }
}