package com.dawn.ai.config;

import com.dawn.ai.tools.*;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configurable
public class AiModuleConfig {
    @Value("${openrouter.base-url}")
    private String baseUrl;

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.chat-model}")
    private String chatModel;

    @Value("${openrouter.embedding-model}")
    private String embeddingModel;

    @Bean
    public ChatModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(chatModel)
                .temperature(0.0)          // tool calling cần deterministic, temperature = 0
                .maxTokens(512)
                .logRequests(true)         // xóa ở production
                .logResponses(true)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(embeddingModel)
                .build();
    }

    @Bean
    public AiAssistant aiAssistant(
            ChatModel chatLanguageModel,
            SearchProductTool searchProductTool,
            GetOrderStatusTool getOrderStatusTool,
            CheckVoucherTool checkVoucherTool,
            CheckCompatibilityTool checkCompatibilityTool,
            CompareProductTool compareProductTool
    ) {
        return AiServices.builder(AiAssistant.class)
                .chatModel(chatLanguageModel)
                // Stateless: không có chatMemory → mỗi call là 1 conversation độc lập
                .tools(
                        searchProductTool,
                        getOrderStatusTool,
                        checkVoucherTool,
                        checkCompatibilityTool,
                        compareProductTool
                )
                .build();
    }
}
