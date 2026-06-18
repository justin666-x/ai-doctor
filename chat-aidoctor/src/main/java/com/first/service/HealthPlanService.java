package com.first.service;

import com.first.pojo.HealthPlan;
import java.util.List;

public interface HealthPlanService {
    void addHealthPlan(HealthPlan healthPlan);
    List<HealthPlan> getHealthPlansByUserId(Integer userId);
    void updateHealthPlan(HealthPlan healthPlan);
    void deleteHealthPlan(Integer id, Integer userId);
    HealthPlan generateAIPlan(Integer userId, String goal);
    void updateEmailReminder(Integer id, Integer userId, Integer emailReminder);
} 