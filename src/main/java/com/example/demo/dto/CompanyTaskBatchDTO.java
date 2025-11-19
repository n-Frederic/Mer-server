package com.example.demo.dto;
import lombok.Data;
import java.util.List;

@Data // 自动生成getter、setter
public class CompanyTaskBatchDTO {
    private List<String> tasks; // 对应前端的"tasks"数组
}