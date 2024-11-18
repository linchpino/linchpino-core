package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.AIService;
import com.linchpino.ai.model.Prompt;
import com.linchpino.ai.model.RequestDetail;
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
    public String talkToAI(RequestDetail requestDetail) {
        return aiClient.generate(Prompt.of(requestDetail).toString());
    }
}
