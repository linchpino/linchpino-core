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

    private final RequestDetail requestDetail;

    public Prompt(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
    }

    public static Prompt of(RequestDetail requestDetail) {
        return new Prompt(requestDetail);
    }

    public static String getRoadmapPromptFor(String fullName, String fieldOfStudy, String currentLevel, String targetLevel, String goal) {
        return Prompt.ROADMAP_PROMPT.replace("_fullName_", fullName)
            .replace("_fieldOfStudy_", fieldOfStudy)
            .replace("_currentLevel_", currentLevel)
            .replace("_targetLevel_", targetLevel)
            .replace("_goal_", goal);
    }

    public String toString() {
        return Prompt.ROADMAP_PROMPT.replace("_fullName_", requestDetail.getFullName())
            .replace("_fieldOfStudy_", requestDetail.getField())
            .replace("_currentLevel_", requestDetail.getLevel())
            .replace("_targetLevel_", requestDetail.getTargetLevel())
            .replace("_goal_", requestDetail.getGoal());
    }

}
