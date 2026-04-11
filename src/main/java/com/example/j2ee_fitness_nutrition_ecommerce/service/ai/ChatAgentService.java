package com.example.j2ee_fitness_nutrition_ecommerce.service.ai;

import com.example.j2ee_fitness_nutrition_ecommerce.config.GeminiConfig;
import com.example.j2ee_fitness_nutrition_ecommerce.dto.ChatResponse;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ai.GeminiApiClient.GeminiResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatAgentService {

    private static final Logger log = LoggerFactory.getLogger(ChatAgentService.class);
    private static final String CHAT_HISTORY_KEY = "chat_history";
    private static final int MAX_TOOL_CALL_LOOPS = 5;

    private final GeminiApiClient geminiApiClient;
    private final AgentToolDefinitions toolDefinitions;
    private final AgentToolExecutor toolExecutor;
    private final GeminiConfig config;

    public ChatAgentService(GeminiApiClient geminiApiClient, AgentToolDefinitions toolDefinitions,
                             AgentToolExecutor toolExecutor, GeminiConfig config) {
        this.geminiApiClient = geminiApiClient;
        this.toolDefinitions = toolDefinitions;
        this.toolExecutor = toolExecutor;
        this.config = config;
    }

    public ChatResponse chat(String userMessage, HttpSession session, String userEmail) {
        // Rate limiting check
        if (isRateLimited(session)) {
            return ChatResponse.builder()
                    .reply("Bạn đang gửi tin nhắn quá nhanh. Vui lòng đợi một chút.")
                    .error(true)
                    .build();
        }

        List<Map<String, Object>> history = getHistory(session);

        // Add user message
        history.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userMessage))
        ));

        String systemPrompt = buildSystemPrompt(userEmail);
        List<ChatResponse.AgentAction> actions = new ArrayList<>();

        // Conversation loop: send to Gemini, handle tool calls, repeat
        for (int i = 0; i < MAX_TOOL_CALL_LOOPS; i++) {
            GeminiResponse response = geminiApiClient.sendMessage(
                    history, toolDefinitions.getToolDefinitions(), systemPrompt);

            if (response.isError()) {
                log.error("Gemini error: {}", response.errorMessage());
                return ChatResponse.builder()
                        .reply("Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau.")
                        .error(true)
                        .build();
            }

            if (response.hasFunctionCall()) {
                String funcName = response.functionName();
                Map<String, Object> funcArgs = response.functionArgs();
                log.info("Agent calling tool: {}({})", funcName, funcArgs);

                // Add model's function call to history
                history.add(response.modelContent());

                // Execute the tool
                AgentToolExecutor.ToolResult result = toolExecutor.execute(
                        funcName, funcArgs, session, userEmail);

                actions.add(new ChatResponse.AgentAction(funcName, result.actionSummary()));

                // Add function response to history
                Map<String, Object> functionResponse = new LinkedHashMap<>();
                functionResponse.put("name", funcName);
                functionResponse.put("response", result.data());

                history.add(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("functionResponse", functionResponse))
                ));

            } else {
                // Text response — we're done
                String reply = response.text();
                history.add(response.modelContent());
                trimHistory(history);
                saveHistory(session, history);

                return ChatResponse.builder()
                        .reply(reply)
                        .actions(actions)
                        .build();
            }
        }

        // Exhausted loop limit
        trimHistory(history);
        saveHistory(session, history);
        return ChatResponse.builder()
                .reply("Tôi đã thực hiện một số thao tác nhưng chưa thể hoàn tất phản hồi. Vui lòng thử lại.")
                .actions(actions)
                .error(true)
                .build();
    }

    public void clearHistory(HttpSession session) {
        session.removeAttribute(CHAT_HISTORY_KEY);
    }

    private String buildSystemPrompt(String userEmail) {
        return """
                You are FitShop AI Assistant — a smart, friendly shopping assistant for FitShop, \
                an e-commerce store specializing in fitness and nutrition supplements \
                (Whey Protein, Mass Gainer, Pre-workout, Vitamins, BCAAs, Creatine, etc.).

                YOUR CAPABILITIES:
                - Search and recommend products based on user needs and fitness goals
                - Show detailed product information including variants, prices, and ratings
                - Add products to the user's shopping cart
                - Check order history and order status
                - Calculate TDEE (Total Daily Energy Expenditure) and recommend macros
                - View the user's wishlist

                RULES:
                1. Be concise and helpful. Use bullet points for product lists.
                2. Format prices in VND (e.g., 850,000₫). Use dot as thousand separator.
                3. When showing products, always include name, brand, and price range.
                4. Before adding to cart: ALWAYS use getProductDetail first to find the correct variantId, \
                   then confirm with the user which variant (flavor/weight) they want.
                5. NEVER fabricate product data. Only use information returned by tools.
                6. Respond in the same language the user uses (Vietnamese or English).
                7. For fitness/nutrition advice, encourage using the TDEE calculator for personalized results.
                8. If you cannot help with something, politely say so and suggest alternatives.
                9. Keep responses under 300 words unless detailed info is needed.

                CURRENT USER: %s
                """.formatted(userEmail);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getHistory(HttpSession session) {
        List<Map<String, Object>> history = (List<Map<String, Object>>) session.getAttribute(CHAT_HISTORY_KEY);
        if (history == null) {
            history = new ArrayList<>();
            session.setAttribute(CHAT_HISTORY_KEY, history);
        }
        return history;
    }

    private void saveHistory(HttpSession session, List<Map<String, Object>> history) {
        session.setAttribute(CHAT_HISTORY_KEY, history);
    }

    private void trimHistory(List<Map<String, Object>> history) {
        int maxHistory = config.getMaxHistory();
        while (history.size() > maxHistory) {
            history.remove(0);
        }
    }

    private boolean isRateLimited(HttpSession session) {
        String key = "chat_rate_timestamps";
        @SuppressWarnings("unchecked")
        List<Long> timestamps = (List<Long>) session.getAttribute(key);
        if (timestamps == null) {
            timestamps = new ArrayList<>();
        }

        long now = System.currentTimeMillis();
        long oneMinuteAgo = now - 60_000;

        // Remove old timestamps
        timestamps.removeIf(t -> t < oneMinuteAgo);

        if (timestamps.size() >= config.getRateLimitPerMinute()) {
            return true;
        }

        timestamps.add(now);
        session.setAttribute(key, timestamps);
        return false;
    }
}
