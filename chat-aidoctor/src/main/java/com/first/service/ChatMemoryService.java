package com.first.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.time.Duration;

@Service
public class ChatMemoryService {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    private final ChatMemory chatMemory;
    public ChatMemoryService(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public Flux<String> chatWithMemoryStream(String conversationId, String message) {
        ChatClient.StreamResponseSpec resp = ChatClient.builder(openAiChatModel)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .build()
                .prompt().user(message)
                .advisors(advisor ->
                        advisor.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                                .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50)
                ).stream();

        return resp.content().timeout(Duration.ofSeconds(90));
    }
}
