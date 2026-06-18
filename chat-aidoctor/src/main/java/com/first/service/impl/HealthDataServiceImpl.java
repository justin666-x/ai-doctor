package com.first.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.first.mapper.HealthDataMapper;
import com.first.pojo.HealthData;
import com.first.service.ChatService;
import com.first.service.HealthDataService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthDataServiceImpl implements HealthDataService {

    @Resource
    private HealthDataMapper healthDataMapper;

    @Resource
    private ChatService chatService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void addHealthData(HealthData data) {
        LocalDateTime now = LocalDateTime.now();
        if (data.getRecordTime() == null) {
            data.setRecordTime(now);
        }
        data.setCreateTime(now);
        data.setUpdateTime(now);
        // 直接插入数据库
        healthDataMapper.add(data);
    }

    @Override
    public List<HealthData> getHealthDataByUser(Integer userId) {
        return healthDataMapper.findByUserId(userId);
    }

    private String generateReportText(HealthData data) {
        // 构建提示词
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下健康检查数据，生成一份专业、简洁且连贯的中文文字版健康分析报告，包含必要的小节说明与整体建议，不要使用任何代码块或JSON格式，只输出纯文字内容。\n\n")
          .append("体温(°C): ").append(data.getTemperature()).append("\n")
          .append("心率(次/分): ").append(data.getHeartRate()).append("\n")
          .append("呼吸频率(次/分): ").append(data.getRespiratoryRate()).append("\n")
          .append("血压(mmHg): ").append(data.getBloodPressure()).append("\n")
          .append("红细胞(10^12/L): ").append(data.getRedBloodCell()).append("\n")
          .append("血红蛋白(g/L): ").append(data.getHemoglobin()).append("\n")
          .append("白细胞(10^9/L): ").append(data.getWhiteBloodCell()).append("\n")
          .append("血小板(10^9/L): ").append(data.getPlatelet()).append("\n")
          .append("空腹血糖(mmol/L): ").append(data.getFastingBloodGlucose()).append("\n")
          .append("餐后2小时血糖(mmol/L): ").append(data.getPostprandialBloodGlucose()).append("\n")
          .append("总胆固醇(mmol/L): ").append(data.getTotalCholesterol()).append("\n")
          .append("甘油三酯(mmol/L): ").append(data.getTriglycerides()).append("\n")
          .append("高密度脂蛋白胆固醇(mmol/L): ").append(data.getHdlCholesterol()).append("\n")
          .append("低密度脂蛋白胆固醇(mmol/L): ").append(data.getLdlCholesterol()).append("\n");

        String prompt = sb.toString();
        return chatService.chat(prompt).trim();
    }

    @Override
    public HealthData analyze(HealthData data) {
        String report = generateReportText(data);
        data.setOverallHealthReport(report);
        return data;
    }

    @Override
    public HealthData analyzeAndSave(HealthData data) {
        String report = generateReportText(data);
        data.setOverallHealthReport(report);
        // 保存
        addHealthData(data);
        return data;
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    @Override
    public void delete(Integer id, Integer userId) {
        healthDataMapper.delete(id, userId);
    }
} 