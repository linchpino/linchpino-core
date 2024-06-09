package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.RoadmapService;
import com.linchpino.ai.service.domain.Prompt;
import org.springframework.ai.client.AiClient;
import org.springframework.stereotype.Service;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    private final AiClient aiClient;
    private final GeminiService geminiService;

    public RoadmapServiceImpl(AiClient aiClient, GeminiService geminiService) {
        this.aiClient = aiClient;
        this.geminiService = geminiService;
    }

    @Override
    public String getRoadmapViaChatgpt() {
        return aiClient.generate(Prompt.getDefaultRoadmapPrompt());
    }

    @Override
    public String getRoadmapViaGemini() {
        return geminiService.callGemini(Prompt.getDefaultRoadmapPrompt());
    }

}
