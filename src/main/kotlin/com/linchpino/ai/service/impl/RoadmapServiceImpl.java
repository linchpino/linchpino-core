package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.RoadmapService;
import com.linchpino.ai.service.domain.Prompt;
import org.apache.coyote.BadRequestException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    private ApplicationContext applicationContext;

    public AIService loadAIService(AIServiceName serviceName) {
        return applicationContext.getBean(serviceName.getComponentName(), AIService.class);
    }

    public String getRoadmap(String aIServiceProviderName) throws BadRequestException {
        AIServiceName serviceName = AIServiceName.fromComponentName(aIServiceProviderName).orElseThrow(() -> new BadRequestException("Invalid AI Service Provider Name"));
        return loadAIService(serviceName).talkToAI(Prompt.getDefaultRoadmapPrompt());
    }

}
