package com.first.pojo;

import lombok.Data;
import java.time.LocalDate;

/**
 * 健康计划实体
 */
@Data
public class HealthPlan {
    private Integer id;
    private Integer userId;
    private String planName;
    private String goal;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    private String cycle;
    private String reminder;
    private String status;
    private String aiAdvice;
    private Integer emailReminder; // 是否开启邮箱提醒（1=开启，0=关闭）
} 