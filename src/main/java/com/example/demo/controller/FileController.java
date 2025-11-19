// FileController.java
package com.example.demo.controller;

import com.example.demo.entity.Login;
import com.example.demo.service.FileService;
import com.example.demo.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private LoginService loginService;

    @GetMapping("/{taskId}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long taskId,
            @PathVariable String filename,
            HttpServletRequest request) {

        try {
            System.out.println("🎯 FileController.downloadFile 被调用:");
            System.out.println("   Task ID: " + taskId);
            System.out.println("   文件名: " + filename);
            System.out.println("   完整URL: " + request.getRequestURL());

            // 手动检查认证
            String token = extractTokenFromRequest(request);
            System.out.println("   Token 存在: " + (token != null));

            if (!isValidToken(token)) {
                System.out.println("❌ 认证失败");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(null);
            }

            System.out.println("✅ 认证通过，开始加载文件");

            // 使用 taskId 和 filename 加载文件
            Resource resource = fileService.loadFileAsResource(taskId, filename);

            // 确定文件类型
            String contentType = determineContentType(filename);

            System.out.println("✅ 文件加载成功:");
            System.out.println("   类型: " + contentType);
            System.out.println("   文件名: " + resource.getFilename());
            System.out.println("   存在: " + resource.exists());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.out.println("❌ FileController 错误: " + e.getMessage());
            e.printStackTrace();

            // 返回更详细的错误信息
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("X-Error-Message", e.getMessage())
                    .body(null);
        }
    }

    // 添加调试端点
    @GetMapping("/debug/storage-info")
    public ResponseEntity<Map<String, Object>> getStorageInfo() {
        try {
            System.out.println("🔍 调试端点被调用: /debug/storage-info");

            Map<String, Object> info = new HashMap<>();

            // 获取当前工作目录
            Path currentDir = Paths.get("").toAbsolutePath();
            info.put("currentWorkingDir", currentDir.toString());
            System.out.println("📁 当前工作目录: " + currentDir);

            // 检查多个可能的存储路径
            String[] possibleDirs = {"./uploads/", "uploads", "./uploads", "../uploads", "../../uploads"};
            List<Map<String, Object>> dirChecks = new ArrayList<>();

            for (String dir : possibleDirs) {
                Map<String, Object> dirInfo = new HashMap<>();
                Path dirPath = Paths.get(dir).toAbsolutePath();
                dirInfo.put("path", dir);
                dirInfo.put("absolutePath", dirPath.toString());
                dirInfo.put("exists", Files.exists(dirPath));

                if (Files.exists(dirPath)) {
                    try {
                        // 检查 task_reports 目录
                        Path taskReportsDir = dirPath.resolve("task_reports");
                        dirInfo.put("taskReportsDirExists", Files.exists(taskReportsDir));

                        // 检查任务13目录
                        Path task13Dir = taskReportsDir.resolve("13");
                        dirInfo.put("task13DirExists", Files.exists(task13Dir));

                        if (Files.exists(task13Dir)) {
                            try {
                                List<String> files = Files.list(task13Dir)
                                        .map(path -> path.getFileName().toString())
                                        .collect(Collectors.toList());
                                dirInfo.put("task13Files", files);

                                // 检查目标文件
                                Path targetFile = task13Dir.resolve("0cd630c2-ea18-403c-b140-8fb4357758c5.png");
                                dirInfo.put("targetFileExists", Files.exists(targetFile));
                                if (Files.exists(targetFile)) {
                                    dirInfo.put("fileSize", Files.size(targetFile));
                                    dirInfo.put("isReadable", Files.isReadable(targetFile));
                                }
                            } catch (Exception e) {
                                dirInfo.put("error", "无法列出文件: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        dirInfo.put("error", "检查子目录失败: " + e.getMessage());
                    }
                }
                dirChecks.add(dirInfo);
            }

            info.put("directoryChecks", dirChecks);

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            System.out.println("❌ 调试端点错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // 添加文件存在性检查端点
    @GetMapping("/debug/check-file/{taskId}/{filename:.+}")
    public ResponseEntity<Map<String, Object>> checkFileExists(
            @PathVariable Long taskId,
            @PathVariable String filename) {

        try {
            System.out.println("🔍 检查文件存在性: taskId=" + taskId + ", filename=" + filename);

            Map<String, Object> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("filename", filename);

            // 尝试多种路径查找文件
            String[] baseDirs = {"./uploads/", "uploads", "./uploads"};
            boolean fileFound = false;

            for (String baseDir : baseDirs) {
                Path basePath = Paths.get(baseDir).toAbsolutePath();
                Path filePath = basePath.resolve("task_reports")
                        .resolve(taskId.toString())
                        .resolve(filename)
                        .normalize();

                Map<String, Object> checkResult = new HashMap<>();
                checkResult.put("baseDir", baseDir);
                checkResult.put("absolutePath", filePath.toString());
                checkResult.put("exists", Files.exists(filePath));

                if (Files.exists(filePath)) {
                    checkResult.put("fileSize", Files.size(filePath));
                    checkResult.put("isReadable", Files.isReadable(filePath));
                    checkResult.put("isDirectory", Files.isDirectory(filePath));
                    fileFound = true;
                }

                result.put("check_" + baseDir.replace("/", "_"), checkResult);
            }

            result.put("fileFound", fileFound);

            if (!fileFound) {
                System.out.println("❌ 文件在所有位置都不存在: " + filename);
            } else {
                System.out.println("✅ 文件找到");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("❌ 检查文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        Optional<Login> loginOpt = loginService.findByToken(token);
        if (loginOpt.isEmpty()) {
            return false;
        }

        Login login = loginOpt.get();
        return !login.isExpired();
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "png": return "image/png";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "gif": return "image/gif";
            case "bmp": return "image/bmp";
            case "webp": return "image/webp";
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default: return "application/octet-stream";
        }
    }
}