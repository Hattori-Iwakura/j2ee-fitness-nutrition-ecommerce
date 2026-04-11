package com.example.j2ee_fitness_nutrition_ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Value("${gemini.api.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${gemini.chat.max-history:20}")
    private int maxHistory;

    @Value("${gemini.chat.rate-limit-per-minute:20}")
    private int rateLimitPerMinute;

    @Bean
    public HttpClient geminiHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }
}
