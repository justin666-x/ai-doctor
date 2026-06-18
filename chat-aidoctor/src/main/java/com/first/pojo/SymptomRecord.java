package com.first.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 症状记录实体
 */
@Data
public class SymptomRecord {
    private Integer id;           // 主键ID
    private Integer userId;       // 关联用户ID
    private String symptomText;   // 用户输入的症状描述
    private String aiAnalysis;    // AI 分析结果
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
} 