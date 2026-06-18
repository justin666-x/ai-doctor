package com.first.mapper;

import com.first.pojo.ChatHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatHistoryMapper {

    @Insert("INSERT INTO chat_history(user_id, title, messages, create_time, update_time) VALUES(#{userId}, #{title}, #{messages}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ChatHistory history);

    @Select("SELECT id, user_id, title, create_time, update_time FROM chat_history WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<ChatHistory> listByUser(@Param("userId") Integer userId);

    @Select("SELECT * FROM chat_history WHERE id = #{id} AND user_id = #{userId}")
    ChatHistory findById(@Param("id") Integer id, @Param("userId") Integer userId);

    @Delete("DELETE FROM chat_history WHERE id = #{id} AND user_id = #{userId}")
    void delete(@Param("id") Integer id, @Param("userId") Integer userId);

    @Update("UPDATE chat_history SET messages = #{messages}, title = #{title}, update_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    void update(ChatHistory history);

    // 删除用户的所有聊天历史
    @Delete("DELETE FROM chat_history WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
} 