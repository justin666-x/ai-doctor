package com.first.service;

import com.first.pojo.MedicationPlan;
import java.util.List;

public interface MedicationPlanService {
    void addPlan(MedicationPlan plan);
    List<MedicationPlan> getPlansByUserId(Integer userId);
    void updatePlan(MedicationPlan plan);
    void deletePlan(Integer id, Integer userId);
} 