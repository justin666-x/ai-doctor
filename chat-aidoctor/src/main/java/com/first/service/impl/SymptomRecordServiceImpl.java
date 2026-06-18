package com.first.service.impl;

import com.first.mapper.SymptomRecordMapper;
import com.first.pojo.SymptomRecord;
import com.first.service.SymptomRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 症状记录服务实现类，实现了症状记录的增、查、删等业务逻辑
 */
@Service
public class SymptomRecordServiceImpl implements SymptomRecordService {

    /** 症状记录Mapper，负责与数据库交互 */
    @Autowired
    private SymptomRecordMapper symptomRecordMapper;

    /**
     * 添加一条新的症状记录
     * @param record 症状记录对象，包含用户ID、症状描述、AI分析等信息
     */
    @Override
    public void addRecord(SymptomRecord record) {
        symptomRecordMapper.insert(record);
    }

    /**
     * 查询指定用户的所有症状记录
     * @param userId 用户ID
     * @return 该用户的症状记录列表
     */
    @Override
    public List<SymptomRecord> listRecords(Integer userId) {
        return symptomRecordMapper.listByUser(userId);
    }

    /**
     * 删除指定用户的指定症状记录
     * @param id 症状记录ID
     * @param userId 用户ID
     */
    @Override
    public void deleteRecord(Integer id, Integer userId) {
        symptomRecordMapper.delete(id, userId);
    }
} 