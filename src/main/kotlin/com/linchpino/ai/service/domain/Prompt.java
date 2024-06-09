package com.linchpino.ai.service.domain;

public class Prompt {

    public static final String ROADMAP_PROMPT = """
        Hi, I'm {_fullName_} and
        I am started my field which is {_fieldOfStudy_} and
        based on estimation my level is {_currentLevel_} and
        I want to have a roadmap to reach to level {_targetLevel_} and goal is {_goal_}
        could you please give me a roadmap to reach goal {_goal_} and level {_targetLevel_}
        please give me road map in json format like below
        {
            "currentLevel": {_currentLevel_},
            "targetLevel": {_targetLevel_},
            "goal": {_goal_},
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

    public static String defaultFullName = "John Doe";
    public static String defaultFieldOfStudy = "Data Science";
    public static String defaultCurrentLevel = "Junior Data Scientist";
    public static String defaultTargetLevel = "Senior Data Scientist";
    public static String defaultGoal = "Data Scientist";

    public static String getRoadmapPrompt(String fullName, String fieldOfStudy, String currentLevel, String targetLevel, String goal) {
        return Prompt.ROADMAP_PROMPT.replace("_fullName_", fullName)
            .replace("_fieldOfStudy_", fieldOfStudy)
            .replace("_currentLevel_", currentLevel)
            .replace("_targetLevel_", targetLevel)
            .replace("_goal_", goal);
    }

    public static String getDefaultRoadmapPrompt() {
        return getRoadmapPrompt(Prompt.defaultFullName, Prompt.defaultFieldOfStudy, Prompt.defaultCurrentLevel, Prompt.defaultTargetLevel, Prompt.defaultGoal);
    }

}
