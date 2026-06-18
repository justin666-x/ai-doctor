package com.first.mapper;

import com.first.pojo.HealthPlan;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface HealthPlanMapper {
    // 新增健康计划
    @Insert("INSERT INTO health_plan (user_id, plan_name, goal, content, start_date, end_date, cycle, reminder, status, ai_advice, email_reminder) VALUES (#{userId}, #{planName}, #{goal}, #{content}, #{startDate}, #{endDate}, #{cycle}, #{reminder}, #{status}, #{aiAdvice}, #{emailReminder})")
    void add(HealthPlan healthPlan);

    // 查询某用户所有健康计划，按创建时间倒序
    @Select("SELECT * FROM health_plan WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<HealthPlan> findByUserId(@Param("userId") Integer userId);

    // 更新健康计划
    @Update("UPDATE health_plan SET plan_name=#{planName}, goal=#{goal}, content=#{content}, start_date=#{startDate}, end_date=#{endDate}, cycle=#{cycle}, reminder=#{reminder}, status=#{status}, ai_advice=#{aiAdvice}, email_reminder=#{emailReminder} WHERE id=#{id} AND user_id=#{userId}")
    void update(HealthPlan healthPlan);

    // 删除健康计划
    @Delete("DELETE FROM health_plan WHERE id=#{id} AND user_id=#{userId}")
    void delete(@Param("id") Integer id, @Param("userId") Integer userId);

    // 删除用户的所有健康计划
    @Delete("DELETE FROM health_plan WHERE user_id=#{userId}")
    void deleteByUserId(@Param("userId") Integer userId);

    // 只更新邮箱提醒开关
    @Update("UPDATE health_plan SET email_reminder=#{emailReminder} WHERE id=#{id} AND user_id=#{userId}")
    void updateEmailReminder(@Param("id") Integer id, @Param("userId") Integer userId, @Param("emailReminder") Integer emailReminder);
} 