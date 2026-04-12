package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.ChatRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.dto.ChatResponse;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ai.ChatAgentService;
import com.example.j2ee_fitness_nutrition_ecommerce.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatAgentService chatAgentService;

    public ChatController(ChatAgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request,
                                              Authentication authentication,
                                              HttpSession session) {
        String userEmail = SecurityUtils.getCurrentUserEmail(authentication);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ChatResponse response = chatAgentService.chat(request.getMessage(), session, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear")
    public ResponseEntity<Void> clearHistory(Authentication authentication,
                                              HttpSession session) {
        if (SecurityUtils.getCurrentUserEmail(authentication) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        chatAgentService.clearHistory(session);
        return ResponseEntity.ok().build();
    }
}
