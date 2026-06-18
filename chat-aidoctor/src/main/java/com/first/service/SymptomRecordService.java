package com.first.service;

import com.first.pojo.SymptomRecord;

import java.util.List;

/**
 * 症状记录服务接口，定义了症状记录的增、查、删等业务操作
 */
public interface SymptomRecordService {
    /**
     * 添加一条新的症状记录
     */
    void addRecord(SymptomRecord record);

    /**
     * 查询指定用户的所有症状记录
     */
    List<SymptomRecord> listRecords(Integer userId);

    /**
     * 删除指定用户的指定症状记录
     */
    void deleteRecord(Integer id, Integer userId);
} 