package com.first.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 健康数据实体，对应 health_data 表
 */
@Data
public class HealthData {
    private Integer id;
    private Integer userId;
    private LocalDateTime recordTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 生命体征
    private Double temperature;              // 体温(°C)
    private Integer heartRate;               // 心率/脉搏(次/分钟)
    private Integer respiratoryRate;         // 呼吸频率(次/分钟)
    private String bloodPressure;            // 血压(mmHg)

    // 血常规
    private Double redBloodCell;             // 红细胞(10^12/L)
    private Double hemoglobin;               // 血红蛋白(g/L)
    private Double whiteBloodCell;           // 白细胞(10^9/L)
    private Double platelet;                 // 血小板(10^9/L)

    // 血糖
    private Double fastingBloodGlucose;      // 空腹血糖(mmol/L)
    private Double postprandialBloodGlucose; // 餐后2小时血糖(mmol/L)

    // 血脂
    private Double totalCholesterol;         // 总胆固醇(mmol/L)
    private Double triglycerides;            // 甘油三酯(mmol/L)
    private Double hdlCholesterol;           // 高密度脂蛋白胆固醇(mmol/L)
    private Double ldlCholesterol;           // 低密度脂蛋白胆固醇(mmol/L)

    // AI 分析结果
    private String vitalSignsAnalysis;
    private String bloodRoutineAnalysis;
    private String bloodGlucoseAnalysis;
    private String bloodLipidAnalysis;
    private String overallHealthReport;
} 