package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.ChatRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.dto.ChatResponse;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ai.ChatAgentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
                                              @AuthenticationPrincipal UserDetails userDetails,
                                              HttpSession session) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = userDetails.getUsername();
        ChatResponse response = chatAgentService.chat(request.getMessage(), session, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear")
    public ResponseEntity<Void> clearHistory(@AuthenticationPrincipal UserDetails userDetails,
                                              HttpSession session) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        chatAgentService.clearHistory(session);
        return ResponseEntity.ok().build();
    }
}
