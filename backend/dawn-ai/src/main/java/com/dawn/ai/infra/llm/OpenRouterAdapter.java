package com.dawn.ai.infra.llm;

import com.dawn.ai.config.AiAssistant;
import com.dawn.ai.port.out.LlmPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenRouterAdapter implements LlmPort {

    private final AiAssistant aiAssistant;

    @Override
    public String chat(String userMessage) {
        return aiAssistant.chat(userMessage);
    }
}
