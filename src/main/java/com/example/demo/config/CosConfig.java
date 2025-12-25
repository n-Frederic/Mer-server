package com.example.demo.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cos")
@Data
@Slf4j
public class CosConfig {
    private String secretId;
    private String secretKey;
    private String bucketName;
    private String region;
    private String baseUrl;

    @Bean
    public COSClient cosClient() {
        try {
            log.info("正在初始化COS客户端...");
            log.info("配置信息: bucket={}, region={}", bucketName, region);

            // 1. 初始化身份信息
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

            // 2. 设置区域
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            clientConfig.setConnectionTimeout(30000);
            clientConfig.setSocketTimeout(30000);

            // 3. 生成cos客户端
            COSClient cosClient = new COSClient(cred, clientConfig);

            log.info("✅ COS客户端初始化成功！");
            return cosClient;

        } catch (Exception e) {
            log.error("❌ COS客户端初始化失败", e);
            throw new RuntimeException("初始化COS客户端失败", e);
        }
    }
}