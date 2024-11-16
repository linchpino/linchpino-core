package com.linchpino.ai.model

class Prompt(private val requestDetail: RequestDetail) {
    override fun toString(): String {
        return roadmapPrompt
            .replace("_targetLevel_", requestDetail.targetLevel)
            .replace("_summary_", requestDetail.resume.summary)
            .replace("_experience_", requestDetail.resume.experience)
    }

    private val roadmapPrompt: String = """
        This is my summary: {_summary_} and this is my experiences: {_experience_}.
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
        """
}
