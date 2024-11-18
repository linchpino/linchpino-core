package com.linchpino.ai.model

import com.linchpino.ai.service.impl.ChatGPTServiceImpl
import com.linchpino.ai.service.impl.GeminiServiceImpl

enum class AIServiceName(val label: String, @JvmField val componentName: String, private val isDefault: Boolean) {
    CHATGPT("ChatGPT", ChatGPTServiceImpl.COMPONENT_NAME, false),
    GEMINI("Gemini", GeminiServiceImpl.COMPONENT_NAME, true),
    NOT_FOUND("Not found", "Not_found", false);

    companion object {
        val default: AIServiceName
            get() {
                for (value in entries) {
                    if (value.isDefault) {
                        return value
                    }
                }
                return NOT_FOUND
            }

        @JvmStatic
        fun getComponentNameOrDefault(componentName: String?): AIServiceName {
            for (value in entries) {
                if (value.componentName.equals(componentName, ignoreCase = true)) {
                    return value
                }
            }
            return default
        }
    }
}
