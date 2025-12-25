package com.example.demo.vo;

import lombok.Data;

@Data
public class FileUploadResult {
    private String originalFilename;
    private String filename;
    private String url;
    private Long fileSize;
    private String fileType;
}