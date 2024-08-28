package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.AIService;
import com.linchpino.ai.service.RoadmapService;
import com.linchpino.ai.service.domain.AIServiceName;
import com.linchpino.ai.service.domain.RequestDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    @Value("${spring.ai.default-service-provider}")
    private String aiServiceName;

    private final ApplicationContext applicationContext;

    public RoadmapServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AIService loadAIService(AIServiceName serviceName) {
        return applicationContext.getBean(serviceName.getComponentName(), AIService.class);
    }

    public String getRoadmap(RequestDetail requestDetail) {
        AIServiceName serviceName = AIServiceName.getComponentNameOrDefault(aiServiceName);
        return loadAIService(serviceName).talkToAI(requestDetail);
    }

}
