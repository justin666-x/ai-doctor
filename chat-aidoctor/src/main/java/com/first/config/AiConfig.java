package com.first.config;

import com.first.service.ApiSettingsService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class AiConfig {

    private final AtomicReference<OpenAiChatModel> modelRef = new AtomicReference<>();

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public OpenAiChatModel openAiChatModel(
            ApiSettingsService apiSettingsService,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.chat.options.model}") String model,
            @Value("${DEEPSEEK_API_KEY:}") String defaultKey) {

        String apiKey = apiSettingsService.getDeepseekKey();
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = defaultKey;
        }

        OpenAiApi api = OpenAiApi.builder().baseUrl(baseUrl).apiKey(apiKey).build();
        OpenAiChatOptions options = OpenAiChatOptions.builder().model(model).build();
        OpenAiChatModel modelInstance = new OpenAiChatModel(api, options);
        modelRef.set(modelInstance);
        return modelInstance;
    }

    public void refreshOpenAiModel(String newApiKey, String baseUrl, String model) {
        OpenAiApi api = OpenAiApi.builder().baseUrl(baseUrl).apiKey(newApiKey).build();
        OpenAiChatOptions options = OpenAiChatOptions.builder().model(model).build();
        modelRef.set(new OpenAiChatModel(api, options));
    }

    public OpenAiChatModel getCurrentModel() {
        return modelRef.get();
    }
}
