package com.first.service.impl;

import com.first.mapper.MedicationPlanMapper;
import com.first.pojo.MedicationPlan;
import com.first.service.MedicationPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicationPlanServiceImpl implements MedicationPlanService {
    @Autowired
    private MedicationPlanMapper medicationPlanMapper;

    @Override
    public void addPlan(MedicationPlan plan) {
        medicationPlanMapper.add(plan);
    }

    @Override
    public List<MedicationPlan> getPlansByUserId(Integer userId) {
        return medicationPlanMapper.findByUserId(userId);
    }

    @Override
    public void updatePlan(MedicationPlan plan) {
        medicationPlanMapper.update(plan);
    }

    @Override
    public void deletePlan(Integer id, Integer userId) {
        medicationPlanMapper.delete(id, userId);
    }
} 