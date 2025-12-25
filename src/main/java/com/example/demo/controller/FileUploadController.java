package com.example.demo.controller;

import com.example.demo.service.CosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final CosService cosService;

    /**
     * 上传文件
     */
    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") Long taskId) {

        log.info("📨 收到文件上传请求: taskId={}, filename={}, size={}",
                taskId, file.getOriginalFilename(), file.getSize());

        try {
            // 1. 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "文件为空"));
            }

            // 2. 生成唯一文件名
            String uuid = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = uuid + extension;

            // 3. 构建COS存储路径
            String cosKey = "tasks/" + taskId + "/" + fileName;

            log.info("📁 文件信息: fileName={}, cosKey={}", fileName, cosKey);

            // 4. 上传到COS
            String fileUrl = cosService.uploadFile(cosKey, file);

            // 5. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("data", Map.of(
                    "taskId", taskId,
                    "fileName", fileName,
                    "originalName", originalFilename,
                    "cosKey", cosKey,
                    "fileUrl", fileUrl,
                    "size", file.getSize()
            ));

            log.info("✅ 文件上传成功返回结果: {}", result);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("文件上传失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());

            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 上传测试接口
     */
    @PostMapping("/test")
    public ResponseEntity<?> testUpload(@RequestParam("file") MultipartFile file) {
        try {
            // 使用一个固定的taskId进行测试
            Long testTaskId = 999L;

            String uuid = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = uuid + extension;
            String cosKey = "test/" + fileName;

            // 上传到COS
            String fileUrl = cosService.uploadFile(cosKey, file);

            // 验证文件是否存在
            boolean exists = cosService.doesObjectExist(cosKey);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "测试上传成功");
            result.put("data", Map.of(
                    "fileName", fileName,
                    "cosKey", cosKey,
                    "fileUrl", fileUrl,
                    "exists", exists,
                    "originalName", originalFilename,
                    "size", file.getSize(),
                    "md5", file.getInputStream().available()
            ));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("测试上传失败", e);
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "测试失败: " + e.getMessage()));
        }
    }

    /**
     * 检查COS连接
     */
    @GetMapping("/test-connection")
    public ResponseEntity<?> testConnection() {
        try {
            // 尝试列出桶中的文件
            String testKey = "test-connection.txt";
            boolean canConnect = cosService.doesObjectExist("nonexistent-key-to-test-connection");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "COS连接正常");
            result.put("data", Map.of(
                    "bucket", cosService.getBucketName(),
                    "connected", true,
                    "timestamp", System.currentTimeMillis()
            ));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("COS连接测试失败", e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "COS连接失败: " + e.getMessage());

            return ResponseEntity.status(500).body(result);
        }
    }
}