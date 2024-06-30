package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.RoadmapService;
import com.linchpino.ai.service.domain.AIServiceName;
import com.linchpino.ai.service.domain.InteractionType;
import com.linchpino.ai.service.domain.RequestDetail;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    private final ApplicationContext applicationContext;

    public RoadmapServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AIService loadAIService(AIServiceName serviceName) {
        return applicationContext.getBean(serviceName.getComponentName(), AIService.class);
    }

    public String getRoadmap(String aIServiceProviderName, String interactionType, RequestDetail requestDetail) {
        AIServiceName serviceName = AIServiceName.getComponentNameOrDefault(aIServiceProviderName);
        InteractionType type = InteractionType.getInteractionTypeOrDefault(interactionType);
        return loadAIService(serviceName).talkToAI(type, requestDetail);
    }

}
