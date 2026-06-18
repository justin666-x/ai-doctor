package com.first.controller;

import com.first.pojo.MedicationPlan;
import com.first.pojo.Result;
import com.first.service.MedicationPlanService;
import com.first.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medication")
public class MedicationPlanController {
    @Autowired
    private MedicationPlanService medicationPlanService;

    @PostMapping("/add")
    public Result add(@RequestBody MedicationPlan plan){
        Integer userId = ThreadLocalUtil.getUserId();
        plan.setUserId(userId);
        medicationPlanService.addPlan(plan);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<MedicationPlan>> list(){
        Integer userId = ThreadLocalUtil.getUserId();
        List<MedicationPlan> list = medicationPlanService.getPlansByUserId(userId);
        return Result.success(list);
    }

    @PutMapping("/update")
    public Result update(@RequestBody MedicationPlan plan){
        Integer userId = ThreadLocalUtil.getUserId();
        plan.setUserId(userId);
        medicationPlanService.updatePlan(plan);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        Integer userId = ThreadLocalUtil.getUserId();
        medicationPlanService.deletePlan(id, userId);
        return Result.success();
    }
} 