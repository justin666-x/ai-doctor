package com.first.controller;

import com.first.pojo.HealthPlan;
import com.first.pojo.Result;
import com.first.service.HealthPlanService;
import com.first.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/healthPlan")
public class HealthPlanController {
    @Autowired
    private HealthPlanService healthPlanService;

    // 新增健康计划
    @PostMapping("/add")
    public Result add(@RequestBody HealthPlan healthPlan) {
        Integer userId = ThreadLocalUtil.getUserId();
        healthPlan.setUserId(userId);
        if (healthPlan.getEmailReminder() == null) {
            healthPlan.setEmailReminder(1); // 默认开启邮箱提醒
        }
        healthPlanService.addHealthPlan(healthPlan);
        return Result.success();
    }

    // 查询当前用户所有健康计划
    @GetMapping("/list")
    public Result<List<HealthPlan>> list() {
        Integer userId = ThreadLocalUtil.getUserId();
        List<HealthPlan> list = healthPlanService.getHealthPlansByUserId(userId);
        return Result.success(list);
    }

    // 更新健康计划
    @PutMapping("/update")
    public Result update(@RequestBody HealthPlan healthPlan) {
        Integer userId = ThreadLocalUtil.getUserId();
        healthPlan.setUserId(userId);
        if (healthPlan.getEmailReminder() == null) {
            healthPlan.setEmailReminder(1); // 默认开启邮箱提醒
        }
        healthPlanService.updateHealthPlan(healthPlan);
        return Result.success();
    }

    // 删除健康计划
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Integer userId = ThreadLocalUtil.getUserId();
        healthPlanService.deleteHealthPlan(id, userId);
        return Result.success();
    }

    // AI自动生成健康计划
    @PostMapping("/generateAI")
    public Result<HealthPlan> generateAI(@RequestBody Map<String, String> params) {
        Integer userId = ThreadLocalUtil.getUserId();
        String goal = params.getOrDefault("goal", "健康生活");
        HealthPlan aiPlan = healthPlanService.generateAIPlan(userId, goal);
        return Result.success(aiPlan);
    }

    // 单独更新邮箱提醒开关
    @PatchMapping("/emailReminder")
    public Result toggleEmailReminder(@RequestBody Map<String,Object> payload) {
        Integer id = (Integer) payload.get("id");
        Integer emailReminder = (Integer) payload.get("emailReminder");
        if(id==null || emailReminder==null) return Result.error("参数错误");
        Map<String,Object> userMap = ThreadLocalUtil.get();
        Integer userId = (Integer) userMap.get("id");
        healthPlanService.updateEmailReminder(id, userId, emailReminder);
        return Result.success();
    }
} 