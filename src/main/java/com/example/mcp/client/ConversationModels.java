package com.example.mcp.client;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public final class ConversationModels {
    private ConversationModels() {}

    public record ChatRequest(
            @NotBlank String conversationId,
            @NotBlank String message) {}

    public record ChatResponse(
            String conversationId,
            String answer,
            int contextCharacters,
            Instant completedAt) {}

    public record Turn(String role, String content, Instant createdAt) {}

    public record ConversationSummary(
            String objective,
            List<String> verifiedFacts,
            List<String> pendingQuestions) {
        public static ConversationSummary empty() {
            return new ConversationSummary("", List.of(), List.of());
        }
    }
}
