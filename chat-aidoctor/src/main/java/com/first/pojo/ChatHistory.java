package com.first.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatHistory {
    private Integer id;
    private Integer userId;
    private String title; // 简短标题
    private String messages; // JSON 格式的消息数组
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 