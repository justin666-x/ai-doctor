package com.first.controller;

import com.first.pojo.HealthData;
import com.first.pojo.Result;
import com.first.service.HealthDataService;
import com.first.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/healthData")
public class HealthDataController {

    @Resource
    private HealthDataService healthDataService;

    /**
     * 保存健康数据（不生成报告）
     */
    @PostMapping("/save")
    public Result save(@RequestBody HealthData data) {
        Integer userId = ThreadLocalUtil.getUserId();
        data.setUserId(userId);
        healthDataService.addHealthData(data);
        return Result.success();
    }

    /**
     * 生成健康分析报告并保存
     */
    @PostMapping("/report")
    public Result<HealthData> generateReport(@RequestBody HealthData data) {
        Integer userId = ThreadLocalUtil.getUserId();
        data.setUserId(userId);
        HealthData result = healthDataService.analyzeAndSave(data);
        return Result.success(result);
    }

    /**
     * 查询当前用户所有健康数据
     */
    @GetMapping("/list")
    public Result<List<HealthData>> list() {
        Integer userId = ThreadLocalUtil.getUserId();
        List<HealthData> list = healthDataService.getHealthDataByUser(userId);
        return Result.success(list);
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Integer userId = ThreadLocalUtil.getUserId();
        healthDataService.delete(id, userId);
        return Result.success();
    }

    // 仅预览，不保存
    @PostMapping("/preview")
    public Result<HealthData> preview(@RequestBody HealthData data) {
        Integer userId = ThreadLocalUtil.getUserId();
        data.setUserId(userId);
        HealthData result = healthDataService.analyze(data);
        return Result.success(result);
    }
} 