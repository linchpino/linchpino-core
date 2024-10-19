package com.linchpino.ai.model

class Roadmap {
    var requestDetail: RequestDetail? = null
    var level: Int = 0
    var targetLevel: Int = 0
    var goal: String? = null
    var steps: List<Step>? = null

    class Step {
        var section: String? = null
        var description: String? = null
        var subSteps: List<SubStep>? = null
    }

    class SubStep {
        var subSection: String? = null
        var description: String? = null
    }
}
