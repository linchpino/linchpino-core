package com.linchpino.ai.service.impl;

import com.linchpino.ai.service.RoadmapService;
import org.springframework.ai.client.AiClient;
import org.springframework.stereotype.Service;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    public static final String ROADMAP_PROMPT = """
        Hi, I'm {fullName} and
        I am started my field which is {field} and
        based on estimation my level is {level} and
        I want to have a roadmap to reach to level {targetLevel} and goal is {goal}
        could you please give me a roadmap to reach goad {goal} and level {targetLevel}
        please give me road map in json format like below
        {
            "roadmap": {
                "level": {level},
                "targetLevel": {targetLevel},
                "goal": {goal},
                "steps": [
                    {
                        "step": 1,
                        "description": "step 1 description",
                        "subSteps": [
                            {
                                "step": 1.1,
                                "description": "step 1.1 description"
                            },
                            {
                                "step": 1.2,
                                "description": "step 1.2 description"
                            }
                        ]
                    }
                ]
            }
        }
        """;

    private final AiClient aiClient;

    public RoadmapServiceImpl(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public String getRoadmap(String fullName, String field, String level, String targetLevel, String goal) {
        return aiClient.generate(ROADMAP_PROMPT.formatted(fullName, field, level, targetLevel, goal));
    }
}
