package com.linchpino.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linchpino.ai.service.domain.Prompt;
import com.linchpino.ai.service.domain.RequestDetail;
import com.linchpino.core.exception.ErrorCode;
import com.linchpino.core.exception.LinchpinException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component("gemini")
public class GeminiServiceImpl implements AIService {

    public static final String COMPONENT_NAME = "gemini";

    @Value("${spring.ai.gemini.api-key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;

    public GeminiServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String talkToAI(RequestDetail requestDetail) {
        try {
            String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s", geminiApiKey);
            // Set the headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Create the request entity
            HttpEntity<String> requestEntity = new HttpEntity<>(getPromptRequest(Prompt.of(requestDetail).toString()), headers);
            // Make the POST request
            ResponseEntity<JsonData> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, JsonData.class);
            JsonData responseJson = response.getBody();
            if (responseJson != null) {
                return responseJson.candidates().get(0).content().parts().get(0).text();
            } else {
                return "Error in response type";
            }
        } catch (Exception e) {
            throw new LinchpinException(ErrorCode.SERVER_ERROR, "Error in generating response from Gemini AI with error: " + e.getMessage(), e);
        }
    }

    private String getPromptRequest(String prompt) {
        ObjectMapper mapper = new ObjectMapper();
        String promptRequest = "";
        ObjectNode contentsJson = mapper.createObjectNode();
        try {
            ObjectNode textJson = mapper.createObjectNode();
            textJson.put("text", prompt);
            ArrayNode partsArray = mapper.createArrayNode();
            partsArray.add(textJson);
            ObjectNode partsJson = mapper.createObjectNode();
            partsJson.set("parts", partsArray);
            ArrayNode contentsArray = mapper.createArrayNode();
            contentsArray.add(partsJson);
            contentsJson.set("contents", contentsArray);
            promptRequest = mapper.writeValueAsString(contentsJson);
        } catch (Exception e) {
            throw new LinchpinException(ErrorCode.SERVER_ERROR, "Error in creating prompt request with error: " + e.getMessage(), e);
        }
        return promptRequest;
    }

    public record Part(String text) {
    }

    public record Content(List<Part> parts, String role) {
    }

    public record SafetyRating(String category, String probability) {
    }

    public record Candidate(Content content, String finishReason, int index, List<SafetyRating> safetyRatings) {
    }

    public record UsageMetadata(int promptTokenCount, int candidatesTokenCount, int totalTokenCount) {
    }

    public record JsonData(List<Candidate> candidates, UsageMetadata usageMetadata) {
    }
}
