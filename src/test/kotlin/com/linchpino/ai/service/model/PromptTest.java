package com.linchpino.ai.service.model;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromptTest {

    @Test
    void getDefaultRoadmapPrompt() {
        String defaultRoadmapPrompt = """
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
        assertEquals(defaultRoadmapPrompt, Prompt.of(RequestDetail.of("Senior Data Scientist", readResumeFile())).toString());
    }

    private File readResumeFile() {
        return new File(getClass().getClassLoader().getResource("ai/pdf/linkedin-profile.pdf").getFile());
    }
}
