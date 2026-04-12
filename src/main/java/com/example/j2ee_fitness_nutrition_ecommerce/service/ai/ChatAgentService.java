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
import java.util.Locale;
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
        boolean cartUpdated = false;
        boolean anyCartAddToolInvoked = false;
        List<String> cartAddFailures = new ArrayList<>();

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
                log.info("Agent calling tool(s): {}", response.functionCalls());

                // Add model's function call(s) to history (full content block from Gemini)
                history.add(response.modelContent());

                List<Map<String, Object>> responseParts = new ArrayList<>();
                for (var call : response.functionCalls()) {
                    String funcName = call.name();
                    Map<String, Object> funcArgs = call.args();
                    AgentToolExecutor.ToolResult result = toolExecutor.execute(
                            funcName, funcArgs, session, userEmail);

                    if ("addToCart".equals(funcName) || "addProductToCart".equals(funcName)) {
                        anyCartAddToolInvoked = true;
                        if (result.success()) {
                            cartUpdated = true;
                        } else {
                            Object err = result.data() != null ? result.data().get("error") : null;
                            if (err != null) {
                                cartAddFailures.add(err.toString());
                            } else {
                                cartAddFailures.add(result.actionSummary());
                            }
                        }
                    }

                    actions.add(new ChatResponse.AgentAction(funcName, result.actionSummary()));

                    Map<String, Object> functionResponse = new LinkedHashMap<>();
                    functionResponse.put("name", funcName);
                    functionResponse.put("response", result.data());
                    if (call.id() != null && !call.id().isBlank()) {
                        functionResponse.put("id", call.id());
                    }
                    responseParts.add(Map.of("functionResponse", functionResponse));
                }

                history.add(Map.of(
                        "role", "user",
                        "parts", responseParts
                ));

            } else {
                // Text response — we're done
                String reply = postProcessAssistantReply(
                        response.text(), userMessage, cartUpdated, anyCartAddToolInvoked, cartAddFailures);
                history.add(response.modelContent());
                trimHistory(history);
                saveHistory(session, history);

                return ChatResponse.builder()
                        .reply(reply)
                        .actions(actions)
                        .cartUpdated(cartUpdated)
                        .build();
            }
        }

        // Exhausted loop limit
        trimHistory(history);
        saveHistory(session, history);
        String reply = postProcessAssistantReply(
                "Tôi đã thực hiện một số thao tác nhưng chưa thể hoàn tất phản hồi. Vui lòng thử lại.",
                userMessage, cartUpdated, anyCartAddToolInvoked, cartAddFailures);
        return ChatResponse.builder()
                .reply(reply)
                .actions(actions)
                .cartUpdated(cartUpdated)
                .error(true)
                .build();
    }

    /**
     * Clarifies when cart tools failed or were never invoked while the user asked to add to cart
     * (reduces misleading "đã thêm giỏ" from the model).
     */
    private String postProcessAssistantReply(String reply, String userMessage,
                                              boolean cartUpdated,
                                              boolean anyCartAddToolInvoked,
                                              List<String> cartAddFailures) {
        if (reply == null) {
            reply = "";
        }
        StringBuilder out = new StringBuilder(reply.trim());
        if (!cartAddFailures.isEmpty()) {
            out.append("\n\n⚠️ **Không thể thêm vào giỏ hàng:** ").append(String.join(" ", cartAddFailures));
        }
        if (userWantsCartAdd(userMessage) && !cartUpdated) {
            if (!anyCartAddToolInvoked) {
                out.append("""
                        
                        
                        ⚠️ **Lưu ý:** Hệ thống chưa ghi nhận lệnh thêm vào giỏ (không có công cụ thêm giỏ chạy thành công). \
                        Hãy thử lại hoặc thêm sản phẩm bằng nút trên trang chi tiết sản phẩm.""");
            }
        }
        return out.toString();
    }

    private static boolean userWantsCartAdd(String msg) {
        if (msg == null || msg.isBlank()) {
            return false;
        }
        String m = msg.toLowerCase(Locale.ROOT);
        if (m.contains("add to cart") || m.contains("add to the cart")) {
            return true;
        }
        if (m.contains("vào giỏ") || m.contains("vao gio")) {
            return true;
        }
        if (m.contains("thêm") && (m.contains("giỏ") || m.contains("gio") || m.contains("cart"))) {
            return true;
        }
        return false;
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
                - Search and browse the real product catalog (searchProducts supports keyword, category slug, pagination)
                - listCategories to discover category slugs before filtering
                - Recommend products (recommendProducts) using live catalog data
                - Show detailed product information including variants, prices, stock, and ratings (getProductDetail)
                - Add products to the cart: prefer addProductToCart(productSlug, optional flavor, optional weight, quantity) \
                  after searchProducts/getProductDetail so you have the real slug; or addToCart(variantId) when you already know the variant ID
                - Check order history and order status
                - Calculate TDEE (Total Daily Energy Expenditure) and recommend macros
                - View the user's wishlist

                RULES:
                1. Be concise and helpful. Use bullet points for product lists.
                2. Format prices in VND (e.g., 850,000₫). Use dot as thousand separator.
                3. When showing products, always include name, brand, and price range.
                4. For any question about what the shop sells, stock, or prices: call searchProducts and/or getProductDetail. \
                   Use searchProducts with empty keyword to browse; use page/pageSize when the user wants more results or "next page".
                5. When the user asks to add items to the cart (e.g. "thêm vào giỏ", "add to cart"): \
                   call searchProducts or getProductDetail to obtain the product slug, then call addProductToCart with that slug. \
                   If they specify flavor or weight, pass them as flavor/weight filters. If ambiguous, ask one short clarifying question \
                   or call getProductDetail and list variants. \
                   NEVER say you added to the cart unless addProductToCart or addToCart returned success in the tool response (success: true).
                6. NEVER fabricate product data. Only use information returned by tools.
                7. Respond in the same language the user uses (Vietnamese or English).
                8. For fitness/nutrition advice, encourage using the TDEE calculator for personalized results.
                9. If you cannot help with something, politely say so and suggest alternatives.
                10. Keep responses under 400 words unless detailed info is needed.

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
