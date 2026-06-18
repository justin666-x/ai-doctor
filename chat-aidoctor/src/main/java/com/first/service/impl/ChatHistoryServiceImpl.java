package com.first.service.impl;

import com.first.mapper.ChatHistoryMapper;
import com.first.pojo.ChatHistory;
import com.first.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Override
    public ChatHistory addHistory(ChatHistory history) {
        chatHistoryMapper.insert(history);
        return history;
    }

    @Override
    public List<ChatHistory> list(Integer userId) {
        return chatHistoryMapper.listByUser(userId);
    }

    @Override
    public ChatHistory get(Integer id, Integer userId) {
        return chatHistoryMapper.findById(id, userId);
    }

    @Override
    public void delete(Integer id, Integer userId) {
        chatHistoryMapper.delete(id, userId);
    }

    @Override
    public void update(ChatHistory history) {
        chatHistoryMapper.update(history);
    }
} 