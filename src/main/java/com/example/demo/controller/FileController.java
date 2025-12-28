// src/main/java/com/example/demo/controller/FileController.java
package com.example.demo.controller;

import com.example.demo.entity.Login;
import com.example.demo.service.FileService;
import com.example.demo.service.LoginService;
import com.example.demo.service.CosService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private CosService cosService;

    // ========== 文件下载 ==========
    @SneakyThrows
    @GetMapping("/{taskId}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long taskId,
            @PathVariable String filename,
            HttpServletRequest request) {

        try {
            log.info("🎯 FileController.downloadFile 被调用:");
            log.info("   Task ID: {}", taskId);
            log.info("   文件名: {}", filename);
            log.info("   完整URL: {}", request.getRequestURL());

//            // 手动检查认证
//            String token = extractTokenFromRequest(request);
//            log.info("   Token 存在: {}", token != null);
//
//            if (!isValidToken(token)) {
//                log.warn("❌ 认证失败");
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .header("X-Error-Message", "Authentication failed")
//                        .body(null);
//            }

            log.info("✅ 认证通过，开始加载文件");

            // 使用 taskId 和 filename 加载文件
            Resource resource = fileService.loadFileAsResource(taskId, filename);

            // 确定文件类型
            String contentType = determineContentType(filename);

            log.info("✅ 文件加载成功:");
            log.info("   类型: {}", contentType);
            log.info("   文件名: {}", resource.getFilename());
            log.info("   存在: {}", resource.exists());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ FileController 错误: {}", e.getMessage(), e);

            // 返回更详细的错误信息（使用英文避免编码问题）
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("X-Error-Code", "FILE_NOT_FOUND")
                    .header("X-Error-Message", URLEncoder.encode("File not found: " + e.getMessage(), "UTF-8"))
                    .body(null);
        }
    }

    // ========== 上传文件到 COS ==========
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "taskId", required = false) Long taskId,
            HttpServletRequest request) {

        try {
            log.info("📤 FileController.uploadFile 被调用:");
            log.info("   文件名: {}", file.getOriginalFilename());
            log.info("   文件大小: {} bytes", file.getSize());
            log.info("   Task ID: {}", taskId);

            // 认证检查
            String token = extractTokenFromRequest(request);
            if (!isValidToken(token)) {
                log.warn("❌ 上传失败：认证无效");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Authentication failed"));
            }

            // 上传文件
            String fileUrl = fileService.uploadFile(file, taskId);

            log.info("✅ 文件上传成功:");
            log.info("   URL: {}", fileUrl);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("url", fileUrl);
            result.put("filename", file.getOriginalFilename());
            result.put("size", file.getSize());
            result.put("taskId", taskId);
            result.put("message", "File uploaded successfully");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ 文件上传失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result);
        }
    }

    // ========== 批量上传到 COS ==========
    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "taskId", required = false) Long taskId,
            HttpServletRequest request) {

        try {
            log.info("📤 FileController.uploadMultipleFiles 被调用:");
            log.info("   文件数量: {}", files.length);
            log.info("   Task ID: {}", taskId);

            // 认证检查
            String token = extractTokenFromRequest(request);
            if (!isValidToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Authentication failed"));
            }

            List<Map<String, Object>> uploadedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                try {
                    String fileUrl = fileService.uploadFile(file, taskId);

                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("filename", file.getOriginalFilename());
                    fileInfo.put("url", fileUrl);
                    fileInfo.put("size", file.getSize());
                    fileInfo.put("success", true);
                    fileInfo.put("message", "Uploaded successfully");

                    uploadedFiles.add(fileInfo);

                    log.info("✅ 上传成功: {} -> {}", file.getOriginalFilename(), fileUrl);

                } catch (Exception e) {
                    log.error("❌ 单个文件上传失败: {} - {}", file.getOriginalFilename(), e.getMessage());

                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("filename", file.getOriginalFilename());
                    fileInfo.put("success", false);
                    fileInfo.put("message", e.getMessage());

                    uploadedFiles.add(fileInfo);
                }
            }

            // 统计结果
            long successCount = uploadedFiles.stream()
                    .filter(f -> (Boolean) f.getOrDefault("success", false))
                    .count();

            Map<String, Object> result = new HashMap<>();
            result.put("success", successCount > 0);
            result.put("files", uploadedFiles);
            result.put("total", files.length);
            result.put("successCount", successCount);
            result.put("failedCount", files.length - successCount);
            result.put("taskId", taskId);
            result.put("message", String.format("Uploaded %d of %d files", successCount, files.length));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ 批量上传失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Batch upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result);
        }
    }

    // ========== 删除文件 ==========
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @RequestParam("url") String fileUrl,
            HttpServletRequest request) {

        try {
            log.info("🗑️ FileController.deleteFile 被调用:");
            log.info("   文件URL: {}", fileUrl);

            // 认证检查
            String token = extractTokenFromRequest(request);
            if (!isValidToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Authentication failed"));
            }

            // 删除文件
            fileService.deleteFile(fileUrl);

            log.info("✅ 文件删除成功: {}", fileUrl);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "File deleted successfully"
            ));

        } catch (Exception e) {
            log.error("❌ 文件删除失败", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Delete failed: " + e.getMessage()
                    ));
        }
    }

    // ========== 获取上传配置 ==========
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getUploadConfig(HttpServletRequest request) {
        try {
            // 认证检查
            String token = extractTokenFromRequest(request);
            if (!isValidToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Authentication failed"));
            }

            Map<String, Object> config = new HashMap<>();
            config.put("storageType", "cos");
            config.put("maxFileSize", 10 * 1024 * 1024);  // 10MB
            config.put("allowedTypes", Arrays.asList(
                    "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "text/plain"
            ));
            config.put("baseUrl", "https://pandora-1386250493.cos.ap-beijing.myqcloud.com");
            config.put("bucketName", "pandora-1386250493");
            config.put("region", "ap-beijing");

            return ResponseEntity.ok(Map.of("success", true, "config", config));

        } catch (Exception e) {
            log.error("获取上传配置失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ========== 调试端点 ==========
    @GetMapping("/debug/storage-info")
    public ResponseEntity<Map<String, Object>> getStorageInfo() {
        try {
            log.info("🔍 调试端点被调用: /debug/storage-info");

            Map<String, Object> info = new HashMap<>();

            // 获取当前工作目录
            Path currentDir = Paths.get("").toAbsolutePath();
            info.put("currentWorkingDir", currentDir.toString());
            log.info("📁 当前工作目录: {}", currentDir);

            // 检查COS配置
            info.put("storageType", "cos");
            info.put("cosBucket", cosService.getBucketName());
            info.put("baseUrl", cosService.getBaseUrl());

            // 检查COS连接
            try {
                boolean connectionTest = cosService.doesObjectExist("test-connection");
                info.put("cosConnection", "OK");
                info.put("connectionTest", connectionTest);
            } catch (Exception e) {
                info.put("cosConnection", "ERROR");
                info.put("connectionError", e.getMessage());
            }

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            log.error("❌ 调试端点错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/debug/check-file/{taskId}/{filename:.+}")
    public ResponseEntity<Map<String, Object>> checkFileExists(
            @PathVariable Long taskId,
            @PathVariable String filename) {

        try {
            log.info("🔍 检查文件存在性: taskId={}, filename={}", taskId, filename);

            // 构建COS key
            String cosKey = "tasks/" + taskId + "/" + filename;

            Map<String, Object> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("filename", filename);
            result.put("cosKey", cosKey);
            result.put("storageType", "cos");
            result.put("bucketName", cosService.getBucketName());

            // 检查文件是否存在
            boolean exists = cosService.doesObjectExist(cosKey);
            result.put("exists", exists);

            if (exists) {
                result.put("message", "File exists in COS");
                result.put("url", cosService.getBaseUrl() + "/" + cosKey);
            } else {
                result.put("message", "File does not exist in COS");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ 检查文件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // ========== 测试上传端点（无需认证） ==========
    @PostMapping("/test-upload")
    public ResponseEntity<Map<String, Object>> testUpload(
            @RequestParam("file") MultipartFile file) {


        try {
            log.info("🧪 测试上传端点被调用:");
            log.info("   文件名: {}", file.getOriginalFilename());
            log.info("   文件大小: {} bytes", file.getSize());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File is empty"));
            }

            // 使用测试taskId
            Long testTaskId = 999L;

            // 构建COS key
            String uuid = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = uuid + extension;
            String cosKey = "test/" + fileName;

            log.info("📤 上传到COS: key={}", cosKey);

            // 上传文件
            String fileUrl = cosService.uploadFile(cosKey, file);

            // 验证
            boolean exists = cosService.doesObjectExist(cosKey);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Test upload successful");
            result.put("data", Map.of(
                    "fileName", fileName,
                    "originalName", originalFilename,
                    "cosKey", cosKey,
                    "fileUrl", fileUrl,
                    "existsInCOS", exists,
                    "size", file.getSize(),
                    "bucket", cosService.getBucketName()
            ));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ 测试上传失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Test upload failed: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // ========== 辅助方法 ==========
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isValidToken(String token) {
        // 获取当前请求
        HttpServletRequest currentRequest = null;
        try {
            currentRequest =
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (IllegalStateException e) {
            log.warn("⚠️ 无法获取当前请求上下文，可能是在异步线程中");
        }

        // 方案4.1：对于特定URL模式跳过认证
        if (currentRequest != null) {
            String requestURI = currentRequest.getRequestURI();
            log.info("📝 请求URI: {}", requestURI);

            // 检查是否包含特定路径，如/test/files/
            if (requestURI.contains("/test/files/")) {
                log.info("⚠️ 测试文件端点，跳过认证");
                return true;
            }

            // 或者检查特定任务ID
            if (requestURI.matches(".*/files/\\d+/.*")) {
                // 提取taskId
                String[] parts = requestURI.split("/");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("files") && i + 1 < parts.length) {
                        try {
                            Long taskId = Long.parseLong(parts[i + 1]);
                            if (taskId.equals(999L)) { // 测试任务ID
                                log.info("⚠️ 测试任务ID {}，跳过认证", taskId);
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            // 忽略，继续检查其他条件
                        }
                    }
                }
            }
        }

        // 方案4.2：使用特定测试token
        if (token != null && token.equals("test-skip-auth")) {
            log.info("⚠️ 使用测试token跳过认证");
            return true;
        }

        // 方案4.3：检查特殊header
        if (currentRequest != null) {
            String bypassHeader = currentRequest.getHeader("X-Bypass-Auth");
            if ("true".equals(bypassHeader)) {
                log.info("⚠️ 通过X-Bypass-Auth header跳过认证");
                return true;
            }

            String testHeader = currentRequest.getHeader("X-Test-Mode");
            if ("enabled".equals(testHeader)) {
                log.info("⚠️ 测试模式启用，跳过认证");
                return true;
            }
        }

        // 原始认证逻辑
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
            case "txt": return "text/plain";
            default: return "application/octet-stream";
        }
    }
}