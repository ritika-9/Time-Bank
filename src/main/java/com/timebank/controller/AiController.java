package com.timebank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AiController {

    @Value("${anthropic.api.key}")
    private String anthropicApiKey;

    @PostMapping("/generate-description")
    public ResponseEntity<Map<String, String>> generateDescription(
            @RequestBody Map<String, String> body) {

        String title = body.get("title");
        String skillName = body.get("skillName");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");

        String prompt = String.format(
                "Generate a clear, concise help request description for someone who needs help with: '%s' (skill: %s). " +
                        "Write 2-3 sentences describing what they need. Be specific and helpful. Just write the description, no preamble.",
                title, skillName
        );

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-sonnet-4-6");
        requestBody.put("max_tokens", 200);
        requestBody.put("messages", List.of(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.anthropic.com/v1/messages",
                    entity,
                    Map.class
            );

            List<Map<String, Object>> content =
                    (List<Map<String, Object>>) response.getBody().get("content");
            String description = (String) content.get(0).get("text");

            return ResponseEntity.ok(Map.of("description", description));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("description",
                    "I need help with " + title + ". Looking for someone experienced with " +
                            skillName + " who can guide me through this."));
        }
    }

    @PostMapping("/match-helpers")
    public ResponseEntity<Map<String, String>> matchHelpers(
            @RequestBody Map<String, String> body) {

        String requestTitle = body.get("title");
        String skillName = body.get("skillName");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");

        String prompt = String.format(
                "Someone needs help with: '%s' (skill: %s). " +
                        "Write a 1-sentence tip on what to look for in a good helper for this task.",
                requestTitle, skillName
        );

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-sonnet-4-6");
        requestBody.put("max_tokens", 100);
        requestBody.put("messages", List.of(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.anthropic.com/v1/messages",
                    entity,
                    Map.class
            );

            List<Map<String, Object>> content =
                    (List<Map<String, Object>>) response.getBody().get("content");
            String tip = (String) content.get(0).get("text");

            return ResponseEntity.ok(Map.of("tip", tip));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("tip",
                    "Look for helpers with strong " + skillName + " skills and good ratings."));
        }
    }
}