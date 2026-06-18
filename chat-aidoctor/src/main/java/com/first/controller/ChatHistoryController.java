package com.first.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.first.pojo.ChatHistory;
import com.first.pojo.Result;
import com.first.service.ChatHistoryService;
import com.first.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 创建历史记录 (包含消息数组) */
    @PostMapping("/add")
    public Result<Integer> add(@RequestBody Map<String, Object> payload) throws JsonProcessingException {
        Integer userId = ThreadLocalUtil.getUserId();
        String title = (String) payload.getOrDefault("title", "未命名会话");
        Object messages = payload.get("messages");
        String messagesJson = messages instanceof String ? (String) messages : objectMapper.writeValueAsString(messages);

        ChatHistory his = new ChatHistory();
        his.setUserId(userId);
        his.setTitle(title);
        his.setMessages(messagesJson);
        chatHistoryService.addHistory(his);
        return Result.success(his.getId());
    }

    @GetMapping("/list")
    public Result<List<ChatHistory>> list() {
        Integer userId = ThreadLocalUtil.getUserId();
        List<ChatHistory> list = chatHistoryService.list(userId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<ChatHistory> get(@PathVariable Integer id) {
        Integer userId = ThreadLocalUtil.getUserId();
        ChatHistory history = chatHistoryService.get(id, userId);
        return Result.success(history);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Integer userId = ThreadLocalUtil.getUserId();
        chatHistoryService.delete(id, userId);
        return Result.success();
    }

    /** 更新历史记录（仅标题/消息） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) throws JsonProcessingException {
        Integer userId = ThreadLocalUtil.getUserId();

        String title = (String) payload.getOrDefault("title", null);
        Object messagesObj = payload.get("messages");

        String messagesJson;
        if (messagesObj instanceof String) {
            messagesJson = (String) messagesObj;
        } else {
            messagesJson = objectMapper.writeValueAsString(messagesObj);
        }

        ChatHistory his = new ChatHistory();
        his.setId(id);
        his.setUserId(userId);
        his.setMessages(messagesJson);
        // 只有当前端传递标题时才更新标题，避免覆盖
        if (title != null) {
            his.setTitle(title);
        }

        chatHistoryService.update(his);
        return Result.success();
    }
} 