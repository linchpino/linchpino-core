package com.linchpino.ai.service.impl;

import org.springframework.ai.client.AiClient;
import org.springframework.stereotype.Component;

@Component("chatgpt")
public class ChatGPTServiceImpl implements AIService {

    public static final String COMPONENT_NAME = "chatgpt";

    private final AiClient aiClient;

    public ChatGPTServiceImpl(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public String talkToAI(String prompt) {
        return aiClient.generate(prompt);
    }

}
