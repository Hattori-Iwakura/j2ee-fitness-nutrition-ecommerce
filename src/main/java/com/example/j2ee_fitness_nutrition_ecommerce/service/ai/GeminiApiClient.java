package com.example.j2ee_fitness_nutrition_ecommerce.service.ai;

import com.example.j2ee_fitness_nutrition_ecommerce.config.GeminiConfig;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiApiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiClient.class);

    private final HttpClient httpClient;
    private final GeminiConfig config;
    private final ObjectMapper objectMapper;

    public GeminiApiClient(HttpClient geminiHttpClient, GeminiConfig config, ObjectMapper objectMapper) {
        this.httpClient = geminiHttpClient;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public GeminiResponse sendMessage(List<Map<String, Object>> contents,
                                      List<Map<String, Object>> tools,
                                      String systemInstruction) {
        try {
            Map<String, Object> requestBody = buildRequestBody(contents, tools, systemInstruction);
            String json = objectMapper.writeValueAsString(requestBody);

            String url = config.getBaseUrl() + "/models/" + config.getModel()
                    + ":generateContent?key=" + config.getApiKey();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API error: status={}, body={}", response.statusCode(), response.body());
                return GeminiResponse.error("Gemini API returned status " + response.statusCode());
            }

            return parseResponse(response.body());
        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            return GeminiResponse.error("Failed to call AI service: " + e.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(List<Map<String, Object>> contents,
                                                   List<Map<String, Object>> tools,
                                                   String systemInstruction) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("contents", contents);

        if (tools != null && !tools.isEmpty()) {
            body.put("tools", tools);
        }

        if (systemInstruction != null && !systemInstruction.isBlank()) {
            body.put("system_instruction", Map.of(
                    "parts", List.of(Map.of("text", systemInstruction))
            ));
        }

        body.put("toolConfig", Map.of(
                "functionCallingConfig", Map.of("mode", "AUTO")
        ));

        body.put("generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 1024
        ));

        return body;
    }

    @SuppressWarnings("unchecked")
    private GeminiResponse parseResponse(String responseBody) {
        try {
            Map<String, Object> root = objectMapper.readValue(responseBody,
                    new TypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) root.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return GeminiResponse.error("No candidates in Gemini response");
            }

            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            if (content == null) {
                return GeminiResponse.error("No content in Gemini response");
            }

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                return GeminiResponse.error("No parts in Gemini response");
            }

            List<GeminiResponse.FunctionCallInvocation> invocations = new ArrayList<>();
            StringBuilder textBuf = new StringBuilder();
            for (Map<String, Object> part : parts) {
                Map<String, Object> functionCall = null;
                if (part.containsKey("functionCall")) {
                    functionCall = (Map<String, Object>) part.get("functionCall");
                } else if (part.containsKey("function_call")) {
                    functionCall = (Map<String, Object>) part.get("function_call");
                }
                if (functionCall != null) {
                    String functionName = functionCall.get("name") != null
                            ? functionCall.get("name").toString() : null;
                    if (functionName == null || functionName.isBlank()) {
                        log.warn("Skipping functionCall with missing name in Gemini response part");
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> args = (Map<String, Object>) functionCall.get("args");
                    if (args == null) {
                        args = Map.of();
                    }
                    String callId = functionCall.get("id") != null ? functionCall.get("id").toString() : null;
                    invocations.add(new GeminiResponse.FunctionCallInvocation(functionName, args, callId));
                }
                Object textObj = part.get("text");
                if (textObj != null) {
                    String t = textObj.toString();
                    if (!t.isBlank()) {
                        if (!textBuf.isEmpty()) {
                            textBuf.append('\n');
                        }
                        textBuf.append(t);
                    }
                }
            }

            if (!invocations.isEmpty()) {
                return GeminiResponse.functionCalls(invocations, content);
            }

            return GeminiResponse.text(textBuf.toString(), content);

        } catch (Exception e) {
            log.error("Failed to parse Gemini response", e);
            return GeminiResponse.error("Failed to parse AI response");
        }
    }

    public record GeminiResponse(
            Type type,
            String text,
            List<FunctionCallInvocation> functionCalls,
            Map<String, Object> modelContent,
            String errorMessage
    ) {
        public enum Type { TEXT, FUNCTION_CALL, ERROR }

        public record FunctionCallInvocation(String name, Map<String, Object> args, String id) {}

        public boolean hasFunctionCall() {
            return type == Type.FUNCTION_CALL && functionCalls != null && !functionCalls.isEmpty();
        }

        public boolean isError() {
            return type == Type.ERROR;
        }

        public static GeminiResponse text(String text, Map<String, Object> modelContent) {
            return new GeminiResponse(Type.TEXT, text, List.of(), modelContent, null);
        }

        public static GeminiResponse functionCalls(List<FunctionCallInvocation> calls, Map<String, Object> modelContent) {
            return new GeminiResponse(Type.FUNCTION_CALL, null, calls, modelContent, null);
        }

        public static GeminiResponse error(String message) {
            return new GeminiResponse(Type.ERROR, null, List.of(), null, message);
        }
    }
}
