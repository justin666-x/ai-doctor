package com.first.mapper;

import com.first.pojo.HealthData;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HealthDataMapper {

    // 新增健康数据
    @Insert("INSERT INTO health_data (user_id, record_time, temperature, heart_rate, respiratory_rate, blood_pressure, red_blood_cell, hemoglobin, white_blood_cell, platelet, fasting_blood_glucose, postprandial_blood_glucose, total_cholesterol, triglycerides, hdl_cholesterol, ldl_cholesterol, overall_health_report, vital_signs_analysis, blood_routine_analysis, blood_glucose_analysis, blood_lipid_analysis, create_time, update_time) VALUES (#{userId}, #{recordTime}, #{temperature}, #{heartRate}, #{respiratoryRate}, #{bloodPressure}, #{redBloodCell}, #{hemoglobin}, #{whiteBloodCell}, #{platelet}, #{fastingBloodGlucose}, #{postprandialBloodGlucose}, #{totalCholesterol}, #{triglycerides}, #{hdlCholesterol}, #{ldlCholesterol}, #{overallHealthReport}, #{vitalSignsAnalysis}, #{bloodRoutineAnalysis}, #{bloodGlucoseAnalysis}, #{bloodLipidAnalysis}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(HealthData data);

    // 查询某用户所有健康数据，按记录时间倒序
    @Select("SELECT * FROM health_data WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<HealthData> findByUserId(@Param("userId") Integer userId);

    // 删除
    @Delete("DELETE FROM health_data WHERE id=#{id} AND user_id=#{userId}")
    void delete(@Param("id") Integer id, @Param("userId") Integer userId);

    // 删除用户的所有健康数据
    @Delete("DELETE FROM health_data WHERE user_id=#{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
} 