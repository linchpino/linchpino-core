package com.linchpino.ai.service.impl

import com.linchpino.ai.model.Prompt
import com.linchpino.ai.model.RequestDetail
import com.linchpino.ai.service.AIService
import org.springframework.ai.client.AiClient
import org.springframework.stereotype.Component

@Component("chatgpt")
class ChatGPTServiceImpl(private val aiClient: AiClient) : AIService {
    override fun talkToAI(requestDetail: RequestDetail): String? {
        return aiClient.generate(Prompt(requestDetail).toString())
    }

    companion object {
        const val COMPONENT_NAME: String = "chatgpt"
    }
}
