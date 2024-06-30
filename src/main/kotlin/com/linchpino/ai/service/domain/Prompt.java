package com.linchpino.ai.service.domain;

public class Prompt {

    public static final String ROADMAP_PROMPT = """
        I want to have a roadmap to reach to level {_targetLevel_}.
        Could you please give me a roadmap to reach level {_targetLevel_}
        Please give me road map in json format like below
        {
            "targetLevel": {_targetLevel_},
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

    private final RequestDetail requestDetail;

    public Prompt(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
    }

    public static Prompt of(RequestDetail requestDetail) {
        return new Prompt(requestDetail);
    }

    public String toString() {
        return Prompt.ROADMAP_PROMPT.replace("_targetLevel_", requestDetail.getTargetLevel());
    }

}
