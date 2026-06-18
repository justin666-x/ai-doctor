package com.first.service.impl;

import com.first.mapper.HealthPlanMapper;
import com.first.pojo.HealthPlan;
import com.first.service.HealthPlanService;
import com.first.utils.XunfeiSparkWebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HealthPlanServiceImpl implements HealthPlanService {
    @Autowired
    private HealthPlanMapper healthPlanMapper;

    @Override
    public void addHealthPlan(HealthPlan healthPlan) {
        healthPlanMapper.add(healthPlan);
    }

    @Override
    public List<HealthPlan> getHealthPlansByUserId(Integer userId) {
        return healthPlanMapper.findByUserId(userId);
    }

    @Override
    public void updateHealthPlan(HealthPlan healthPlan) {
        healthPlanMapper.update(healthPlan);
    }

    @Override
    public void deleteHealthPlan(Integer id, Integer userId) {
        healthPlanMapper.delete(id, userId);
    }

    @Override
    public void updateEmailReminder(Integer id, Integer userId, Integer emailReminder) {
        healthPlanMapper.updateEmailReminder(id, userId, emailReminder);
    }

    // AI生成健康计划
    @Override
    public HealthPlan generateAIPlan(Integer userId, String goal) {
        HealthPlan plan = new HealthPlan();
        plan.setUserId(userId);
        plan.setPlanName("AI智能健康计划");
        plan.setGoal(goal);
       
        String appid = "7e4dd958";
        String apikey = "5e13ecaa8e08772728d2652205cc149f";
        String apisecret = "MjNhZDRjM2RlZjdkMDM0Njg2YWQyZDYz";
        String wsUrl = "wss://spark-api.xf-yun.com/v3.1/chat";
        String prompt = "请只输出3到10条随机的简洁健康计划建议，每条建议不超过20字，不要输出任何解释、理由或思考过程。每条建议前加数字序号，每条建议独占一行。健康目标：【" + goal + "】。";
        String requestJson = "{\"header\":{\"app_id\":\"" + appid + "\"},\"payload\":{\"message\":{\"text\":[{\"role\":\"user\",\"content\":\"" + prompt + "\"}]}},\"parameter\":{\"chat\":{\"domain\":\"generalv3\",\"max_tokens\":4096,\"temperature\":0.5}}}";
        XunfeiSparkWebSocketClient client = new XunfeiSparkWebSocketClient(appid, apikey, apisecret, wsUrl);
        String content;
        String aiAdvice;
        try {
            content = client.generate(requestJson);
            aiAdvice = "本计划由AI大模型根据您的健康目标自动生成，建议结合自身实际情况调整。";
        } catch (Exception e) {
            content = "AI生成异常：" + e.getMessage();
            aiAdvice = "AI生成异常";
        }
        plan.setContent(content);
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusMonths(1));
        plan.setCycle("每日");
        plan.setReminder("每天08:00提醒");
        plan.setStatus("进行中");
        plan.setAiAdvice(aiAdvice);
        return plan;
    }
} 