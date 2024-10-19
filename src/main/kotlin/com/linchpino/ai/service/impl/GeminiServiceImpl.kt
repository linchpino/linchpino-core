package com.linchpino.ai.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.ai.model.Prompt.Companion.of
import com.linchpino.ai.model.RequestDetail
import com.linchpino.ai.service.AIService
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component("gemini")
class GeminiServiceImpl : AIService {
    private val logger: Logger = LoggerFactory.getLogger(GeminiServiceImpl::class.java)

    @Value("\${spring.ai.gemini.api-key}")
    private val geminiApiKey: String? = null

    private val restTemplate = RestTemplate()

    override fun talkToAI(requestDetail: RequestDetail?): String? {
        try {
            val url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s",
                geminiApiKey
            )
            // Set the headers
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            // Create the request entity
            val requestEntity = HttpEntity(getPromptRequest(getPrompt(requestDetail)), headers)
            // Make the POST request
            val response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity,
                JsonData::class.java
            )
            val responseJson = response.body
            return if (responseJson != null) {
                responseJson.candidates[0].content.parts[0].text
            } else {
                "Error in response type"
            }
        } catch (e: Exception) {
            throw LinchpinException(
                ErrorCode.SERVER_ERROR,
                "Error in generating response from Gemini AI with error: " + e.message,
                e
            )
        }
    }

    private fun getPrompt(requestDetail: RequestDetail?): String {
        val prompt = of(requestDetail).toString()
        logger.info("Prompt: {}", prompt)
        return prompt
    }

    private fun getPromptRequest(prompt: String): String {
        val mapper = ObjectMapper()
        var promptRequest = ""
        val contentsJson = mapper.createObjectNode()
        try {
            val textJson = mapper.createObjectNode()
            textJson.put("text", prompt)
            val partsArray = mapper.createArrayNode()
            partsArray.add(textJson)
            val partsJson = mapper.createObjectNode()
            partsJson.set<JsonNode>("parts", partsArray)
            val contentsArray = mapper.createArrayNode()
            contentsArray.add(partsJson)
            contentsJson.set<JsonNode>("contents", contentsArray)
            promptRequest = mapper.writeValueAsString(contentsJson)
        } catch (e: Exception) {
            throw LinchpinException(
                ErrorCode.SERVER_ERROR,
                "Error in creating prompt request with error: " + e.message,
                e
            )
        }
        return promptRequest
    }

    @JvmRecord
    data class Part(val text: String)

    @JvmRecord
    data class Content(val parts: List<Part>, val role: String)

    @JvmRecord
    data class SafetyRating(val category: String, val probability: String)

    @JvmRecord
    data class Candidate(
        val content: Content,
        val finishReason: String,
        val index: Int,
        val safetyRatings: List<SafetyRating>
    )

    @JvmRecord
    data class UsageMetadata(val promptTokenCount: Int, val candidatesTokenCount: Int, val totalTokenCount: Int)

    @JvmRecord
    data class JsonData(val candidates: List<Candidate>, val usageMetadata: UsageMetadata)
    companion object {
        const val COMPONENT_NAME: String = "gemini"
    }
}
