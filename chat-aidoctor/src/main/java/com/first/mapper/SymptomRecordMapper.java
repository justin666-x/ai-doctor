package com.first.mapper;

import com.first.pojo.SymptomRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * SymptomRecord（症状记录）表的Mapper接口，定义了对症状记录表的增、查、删操作
 */
@Mapper
public interface SymptomRecordMapper {

    /**
     * 插入一条新的症状记录
     */
    @Insert("INSERT INTO symptom_record(user_id, symptom_text, ai_analysis, create_time, update_time) " +
            "VALUES(#{userId}, #{symptomText}, #{aiAnalysis}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SymptomRecord record);

    /**
     * 查询指定用户的所有症状记录，按创建时间倒序排列
     */
    @Select("SELECT * FROM symptom_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<SymptomRecord> listByUser(@Param("userId") Integer userId);

    /**
     * 删除指定用户的指定症状记录
     */
    @Delete("DELETE FROM symptom_record WHERE id = #{id} AND user_id = #{userId}")
    void delete(@Param("id") Integer id, @Param("userId") Integer userId);

    // 删除用户的所有症状记录
    @Delete("DELETE FROM symptom_record WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
} 