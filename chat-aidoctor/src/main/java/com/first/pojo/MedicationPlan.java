package com.first.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用药计划实体
 */
@Data
public class MedicationPlan {
    private Integer id;
    private Integer userId;
    private String drugName;    // 药品名称
    private String purpose;     // 药品用途
    private Integer dosageCount; // 每日用药次数
    private String timePoints;  // 用药时间点，格式 HH:mm,HH:mm
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 