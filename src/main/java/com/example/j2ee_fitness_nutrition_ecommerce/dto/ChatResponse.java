package com.example.j2ee_fitness_nutrition_ecommerce.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String reply;

    @Builder.Default
    private List<AgentAction> actions = new ArrayList<>();

    @Builder.Default
    private boolean error = false;

    /** True only if addToCart or addProductToCart ran successfully in this request. */
    @Builder.Default
    private boolean cartUpdated = false;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentAction {
        private String tool;
        private String summary;
    }
}
