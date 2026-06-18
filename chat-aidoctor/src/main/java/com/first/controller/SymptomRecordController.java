package com.first.controller;

import com.first.pojo.Result;
import com.first.pojo.SymptomRecord;
import com.first.service.SymptomRecordService;
import com.first.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 症状记录接口
 */
@RestController
@RequestMapping("/symptom")
public class SymptomRecordController {

    @Autowired
    private SymptomRecordService symptomRecordService;

    /**
     * 新增症状记录
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody SymptomRecord record) {
        Integer userId = ThreadLocalUtil.getUserId();
        record.setUserId(userId);
        symptomRecordService.addRecord(record);
        return Result.success();
    }

    /**
     * 查询当前用户症状记录
     */
    @GetMapping("/list")
    public Result<List<SymptomRecord>> list() {
        Integer userId = ThreadLocalUtil.getUserId();
        List<SymptomRecord> list = symptomRecordService.listRecords(userId);
        return Result.success(list);
    }

    /**
     * 删除症状记录
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Integer userId = ThreadLocalUtil.getUserId();
        symptomRecordService.deleteRecord(id, userId);
        return Result.success();
    }
} 