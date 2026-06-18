package com.first.service;

import com.first.pojo.HealthData;

import java.util.List;

public interface HealthDataService {
    void addHealthData(HealthData data);

    List<HealthData> getHealthDataByUser(Integer userId);

    /**
     * 生成健康数据分析并保存结果
     * @param data 原始健康数据
     * @return 包含分析后的健康数据
     */
    HealthData analyzeAndSave(HealthData data);

    /**
     * 仅生成分析，不保存数据库
     */
    HealthData analyze(HealthData data);

    void delete(Integer id, Integer userId);
} 