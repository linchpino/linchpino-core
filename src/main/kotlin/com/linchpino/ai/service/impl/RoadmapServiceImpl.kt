package com.linchpino.ai.service.impl;

import com.linchpino.ai.model.AIServiceName;
import com.linchpino.ai.model.RequestDetail;
import com.linchpino.ai.model.Resume;
import com.linchpino.ai.service.AIService;
import com.linchpino.ai.service.RoadmapService;
import io.grpc.netty.shaded.io.netty.util.internal.ObjectUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    @Value("${spring.ai.default-service-provider}")
    private String aiServiceName;

    private final ApplicationContext applicationContext;
    private final ResumeService resumeService;

    public RoadmapServiceImpl(ApplicationContext applicationContext, ResumeService resumeService) {
        this.applicationContext = applicationContext;
        this.resumeService = resumeService;
    }

    public AIService loadAIService(AIServiceName serviceName) {
        return applicationContext.getBean(serviceName.getComponentName(), AIService.class);
    }

    public String getRoadmap(String target, File resumeFile) {
        ObjectUtil.checkNotNull(target, "Target level cannot be null");
        ObjectUtil.checkNotNull(resumeFile, "Resume file cannot be null");
        AIServiceName serviceName = AIServiceName.getComponentNameOrDefault(aiServiceName);
        Resume resume = resumeService.create(resumeFile);
        RequestDetail requestDetail = new RequestDetail(target, resumeService.save(resume));
        return loadAIService(serviceName).talkToAI(requestDetail);
    }

}
