package com.linchpino.ai.service.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PromptTest {

    private final String DEFAULT_ROADMAP_PROMPT = """
        Hi, I'm {John Doe} and
        I am started my field which is {Data Science} and
        based on estimation my level is {Junior Data Scientist} and
        I want to have a roadmap to reach to level {Senior Data Scientist} and goal is {Data Scientist}
        could you please give me a roadmap to reach goal {Data Scientist} and level {Senior Data Scientist}
        please give me road map in json format like below
        {
            "currentLevel": {Junior Data Scientist},
            "targetLevel": {Senior Data Scientist},
            "goal": {Data Scientist},
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
        provide me response in json without any other information
        """;

    private final String testPrompt = """
        Hi, I'm {Test} and
        I am started my field which is {Software tester} and
        based on estimation my level is {Junior Test Engineer} and
        I want to have a roadmap to reach to level {Senior Test Engineer} and goal is {Test engineer}
        could you please give me a roadmap to reach goal {Test engineer} and level {Senior Test Engineer}
        please give me road map in json format like below
        {
            "currentLevel": {Junior Test Engineer},
            "targetLevel": {Senior Test Engineer},
            "goal": {Test engineer},
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
        provide me response in json without any other information
        """;

    @Test
    void getRoadmapPromptFor() {
        assertEquals(testPrompt, Prompt.getRoadmapPromptFor("Test", "Software tester", "Junior Test Engineer", "Senior Test Engineer", "Test engineer"));
    }

    @Test
    void getDefaultRoadmapPrompt() {
        assertEquals(DEFAULT_ROADMAP_PROMPT, Prompt.of(RequestDetail.getDefaultRequestDetail()).toString());
    }
}
