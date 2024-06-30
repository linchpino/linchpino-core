package com.linchpino.ai.service.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromptTest {

    @Test
    void getDefaultRoadmapPrompt() {
        String DEFAULT_ROADMAP_PROMPT = """
            I want to have a roadmap to reach to level {Senior Data Scientist}.
            Could you please give me a roadmap to reach level {Senior Data Scientist}
            Please give me road map in json format like below
            {
                "targetLevel": {Senior Data Scientist},
                "steps": [
                    {
                        "step": "1",
                        "description": "step 1 description",
                        "subSteps": [
                            {
                                "subStep": "1.1",
                                "description": "step 1.1 description"
                            },
                            {
                                "subStep": "1.2",
                                "description": "step 1.2 description"
                            }
                        ]
                    }
                ]
            }
            Provide me response in json without any other information.
            """;
        assertEquals(DEFAULT_ROADMAP_PROMPT, Prompt.of(RequestDetail.getDefaultRequestDetail()).toString());
    }
}
