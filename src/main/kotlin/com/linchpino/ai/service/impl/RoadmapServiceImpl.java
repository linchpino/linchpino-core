package com.linchpino.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linchpino.ai.service.RoadmapService;
import org.springframework.ai.client.AiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    public static final String ROADMAP_PROMPT = """
        Hi, I'm {fullName} and
        I am started my field which is {field} and
        based on estimation my level is {currentLevel} and
        I want to have a roadmap to reach to level {targetLevel} and goal is {goal}
        could you please give me a roadmap to reach goal {goal} and level {targetLevel}
        please give me road map in json format like below
        {
            "roadmap": {
                "level": {currentLevel},
                "targetLevel": {targetLevel},
                "goal": {goal},
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
        }
        provide me response in json without any other information
        """;

    @Value("${spring.ai.gemini.api-key}")
    private String geminiApiKey;

    private final AiClient aiClient;

    // Create a new RestTemplate instance
    RestTemplate restTemplate = new RestTemplate();

    public RoadmapServiceImpl(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public String getRoadmapViaChatgpt() {
        var fullName = "John Doe";
        var field = "Data Science";
        var currentLevel = "Junior Data Scientist";
        var targetLevel = "Senior Data Scientist";
        var goal = "Data Scientist";
        return aiClient.generate(getRoadmapPrompt(fullName, field, currentLevel, targetLevel, goal));
    }

    public String getRoadmapViaGemini() {
        var fullName = "Homayoun";
        var field = "Java Web Developer";
        var currentLevel = "Junior Java Developer";
        var targetLevel = "Senior Java Developer";
        var goal = "Java Developer";
        return callGemini(getRoadmapPrompt(fullName, field, currentLevel, targetLevel, goal));
    }


    private String getRoadmapPrompt(String fullName, String field, String currentLevel, String targetLevel, String goal) {
        return ROADMAP_PROMPT.replace("fullName", fullName)
            .replace("field", field)
            .replace("currentLevel", currentLevel)
            .replace("targetLevel", targetLevel)
            .replace("fullName", goal);
    }

    private String callGemini(String prompt) {
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s", geminiApiKey);
        // Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject contentsJson = new JSONObject();
        try {
            JSONObject textJson = new JSONObject();
            textJson.put("text", prompt);
            JSONObject partsJson = new JSONObject();
            partsJson.put("parts", new JSONArray().put(textJson));
            contentsJson.put("contents", new JSONArray().put(partsJson));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the request entity
        HttpEntity<String> requestEntity = new HttpEntity<>(contentsJson.toString(), headers);

        // Make the POST request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonData data;
        try {
            data = mapper.readValue(response.getBody(), JsonData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return data.candidates().get(0).content().parts().get(0).text();
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
