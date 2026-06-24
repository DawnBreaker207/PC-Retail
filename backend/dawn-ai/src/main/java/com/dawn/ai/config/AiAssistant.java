package com.dawn.ai.config;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiAssistant {
    @SystemMessage("""
            """)
    String chat(@UserMessage String userMessage);
}
