// src/main/java/com/example/demo/service/CosService.java
package com.example.demo.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@Getter  // 添加这个注解，自动生成getter方法
public class CosService {

    @Autowired
    private COSClient cosClient;

    @Value("${cos.bucketName}")
    private String bucketName;  // 现在是public访问（通过getter）

    @Value("${cos.baseUrl}")
    private String baseUrl;

    private TransferManager transferManager;

    @PostConstruct
    public void init() {
        log.info("🚀 CosService初始化...");
        log.info("📦 Bucket名称: {}", bucketName);
        log.info("🌐 Base URL: {}", baseUrl);

        try {
            // 测试连接
            testConnection();
            log.info("✅ CosService初始化成功");
        } catch (Exception e) {
            log.error("❌ CosService初始化失败", e);
        }
    }

    private void testConnection() {
        try {
            log.info("测试COS连接...");
            // 简单的连接测试，尝试列出文件
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(bucketName);
            listObjectsRequest.setMaxKeys(1);

            ObjectListing objectListing = cosClient.listObjects(listObjectsRequest);
            log.info("✅ COS连接测试成功，桶中有 {} 个文件",
                    objectListing.getObjectSummaries().size());

        } catch (Exception e) {
            log.warn("⚠️ COS连接测试警告: {}", e.getMessage());
        }
    }

    /**
     * 上传文件到COS（核心方法）
     */
    public String uploadFile(String key, MultipartFile file) {
        log.info("🚀 开始上传文件到COS: key={}, filename={}, size={} bytes",
                key, file.getOriginalFilename(), file.getSize());

        try {
            // 创建元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());

            // 设置Content-Type
            String contentType = file.getContentType();
            if (contentType != null) {
                metadata.setContentType(contentType);
            }

            log.info("📤 上传到桶: {}, Key: {}", bucketName, key);

            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    key,
                    file.getInputStream(),
                    metadata
            );

            // 执行上传
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            String etag = putObjectResult.getETag();

            // 构建文件URL
            String fileUrl = baseUrl + "/" + key;

            log.info("✅ 文件上传成功: key={}, ETag={}, url={}", key, etag, fileUrl);

            // 验证文件确实存在
            boolean exists = doesObjectExist(key);
            log.info("📌 上传后验证文件存在性: key={}, exists={}", key, exists);

            return fileUrl;

        } catch (Exception e) {
            log.error("❌ 文件上传失败: key={}, error={}", key, e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean doesObjectExist(String key) {
        try {
            boolean exists = cosClient.doesObjectExist(bucketName, key);
            log.debug("检查文件存在性: key={}, exists={}", key, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查文件存在性失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String key) {
        log.info("开始下载文件: bucket={}, key={}", bucketName, key);

        try {
            // 先检查文件是否存在
            if (!doesObjectExist(key)) {
                log.error("文件不存在: key={}", key);
                throw new RuntimeException("文件不存在: " + key);
            }

            // 下载文件
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
            COSObject cosObject = cosClient.getObject(getObjectRequest);

            log.info("✅ 文件下载成功: key={}", key);
            return cosObject.getObjectContent();

        } catch (Exception e) {
            log.error("❌ 文件下载失败: key={}, error={}", key, e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件元数据
     */
    public ObjectMetadata getObjectMetadata(String key) {
        try {
            return cosClient.getObjectMetadata(bucketName, key);
        } catch (Exception e) {
            log.error("获取文件元数据失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String key) {
        try {
            cosClient.deleteObject(bucketName, key);
            log.info("文件删除成功: key={}", key);
        } catch (Exception e) {
            log.error("文件删除失败: key={}", key, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * 根据URL删除文件
     */
    public void deleteFileByUrl(String fileUrl) {
        try {
            // 从URL中提取key
            String key = extractKeyFromUrl(fileUrl);
            deleteFile(key);
        } catch (Exception e) {
            log.error("根据URL删除文件失败: url={}", fileUrl, e);
            throw new RuntimeException("删除文件失败", e);
        }
    }

    /**
     * 从URL中提取key
     */
    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            throw new RuntimeException("无效的文件URL: " + fileUrl);
        }
        return fileUrl.substring(baseUrl.length() + 1);
    }
}