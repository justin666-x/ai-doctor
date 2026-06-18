package com.first.mapper;

import com.first.pojo.MedicationPlan;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MedicationPlanMapper {
    @Insert("INSERT INTO medication_plan (user_id, drug_name, purpose, dosage_count, time_points, create_time, update_time) VALUES (#{userId}, #{drugName}, #{purpose}, #{dosageCount}, #{timePoints}, NOW(), NOW())")
    void add(MedicationPlan plan);

    @Select("SELECT * FROM medication_plan WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<MedicationPlan> findByUserId(@Param("userId") Integer userId);

    @Update("UPDATE medication_plan SET drug_name=#{drugName}, purpose=#{purpose}, dosage_count=#{dosageCount}, time_points=#{timePoints}, update_time=NOW() WHERE id=#{id} AND user_id=#{userId}")
    void update(MedicationPlan plan);

    @Delete("DELETE FROM medication_plan WHERE id=#{id} AND user_id=#{userId}")
    void delete(@Param("id") Integer id, @Param("userId") Integer userId);

    // 删除用户的所有用药计划
    @Delete("DELETE FROM medication_plan WHERE user_id=#{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
} 