package com.linchpino.ai.service

import com.linchpino.ai.model.RequestDetail

interface AIService {
    fun talkToAI(requestDetail: RequestDetail?): String?
}
