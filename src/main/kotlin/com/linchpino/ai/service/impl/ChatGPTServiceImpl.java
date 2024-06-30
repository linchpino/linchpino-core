package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.domain.InteractionType;
import com.linchpino.ai.service.domain.Prompt;
import com.linchpino.ai.service.domain.RequestDetail;
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
    public String talkToAI(InteractionType interactionType, RequestDetail requestDetail) {
        if(interactionType.isFunctionCall()) {
            return "Not developed";
        } else if (interactionType.isPrompt()) {
            return getPromptResponse(Prompt.of(requestDetail).toString());
        } else {
            return "Not supported";
        }
    }

    private String getPromptResponse(String prompt) {
        return aiClient.generate(prompt);
    }

}
