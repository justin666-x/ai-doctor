package com.first.service;

import lombok.Getter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Getter
@Service
public class ChatService {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    public Flux<String> chatWithStream(String message) {
        return openAiChatModel.stream(message);
    }

    public String chat(String message) {
        return openAiChatModel.call(message);
    }
}
