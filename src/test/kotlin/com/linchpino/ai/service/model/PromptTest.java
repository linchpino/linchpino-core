package com.linchpino.ai.service.model;

import com.linchpino.ai.model.Prompt;
import com.linchpino.ai.model.RequestDetail;
import com.linchpino.ai.model.Resume;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromptTest {

    @Test
    void getDefaultRoadmapPrompt() {
        String defaultRoadmapPrompt = """
            This is my summary: {I am a java developer. Besides, I am an easy-going person and have a sense of humor. Most of the time, I prefer to be at peace with the world. The others say I am on time for appointments and deadlines. Exploring the world and learning new things makes me feel excited and lively.} and this is my experiences: {APS Group Software Engineer August 2023 - Present (1 year 1 month) Netherlands Zitel  Java Software Developer August 2022 - August 2023 (1 year 1 month) Tehran, Tehran, Iran Sadad Informatics Corporation 1 year 1 month Development Team Lead March 2022 - August 2022 (6 months) Tehran, Iran Java Developer August 2021 - March 2022 (8 months) Tehran, Iran Teanab parto shargh Java Developer September 2018 - August 2021 (3 years) Tehran Province, Iran   Page 1 of 2     Tarahan Novin Software Developer August 2014 - June 2018 (3 years 11 months) Tehran, Iran I used to develop windows applications by C#. Besides, I have developed a Nodejs application and web applications with ExpresJs Before I focus on Java professionally.}.
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
        assertEquals(defaultRoadmapPrompt, Prompt.of(new RequestDetail("Senior Data Scientist", new Resume(null, null))).toString());
    }
}
