package com.first.service;

import com.first.pojo.ChatHistory;

import java.util.List;

public interface ChatHistoryService {
    ChatHistory addHistory(ChatHistory history);           // 添加聊天历史
    List<ChatHistory> list(Integer userId);                // 获取用户聊天列表
    ChatHistory get(Integer id, Integer userId);           // 获取单条记录
    void delete(Integer id, Integer userId);               // 删除记录
    void update(ChatHistory history);                      // 更新记录
} 