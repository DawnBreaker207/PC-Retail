package com.dawn.ai.config;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AiAssistant {
    @SystemMessage("""
            """)
    String chat(@UserMessage String userMessage);
}
