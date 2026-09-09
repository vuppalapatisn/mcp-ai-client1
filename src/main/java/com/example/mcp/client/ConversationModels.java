package com.example.mcp.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;

public final class ConversationModels {
    private ConversationModels() {}

    @Schema(description = "A user message to answer in the context of an ongoing conversation.")
    public record ChatRequest(
            @Schema(description = "Caller-chosen conversation identifier.", example = "c-1001")
            @NotBlank String conversationId,
            @Schema(description = "The user's message.", example = "Where is order 4711?")
            @NotBlank String message) {}

    @Schema(description = "The assistant's answer plus context accounting.")
    public record ChatResponse(
            @Schema(example = "c-1001") String conversationId,
            @Schema(example = "Order 4711 shipped on Tuesday.") String answer,
            @Schema(description = "Characters of prompt context assembled for this turn.", example = "482")
            int contextCharacters,
            Instant completedAt) {}

    @Schema(description = "A single conversation turn.")
    public record Turn(
            @Schema(description = "Who produced the turn.", example = "user",
                    allowableValues = {"user", "assistant"}) String role,
            @Schema(example = "Where is order 4711?") String content,
            Instant createdAt) {}

    @Schema(description = "Running summary carried across a long conversation.")
    public record ConversationSummary(
            @Schema(example = "Resolve a late delivery for order 4711.") String objective,
            @Schema(example = "[\"Order 4711 shipped on Tuesday\"]") List<String> verifiedFacts,
            @Schema(example = "[\"Which address should it be redirected to?\"]") List<String> pendingQuestions) {
        public static ConversationSummary empty() {
            return new ConversationSummary("", List.of(), List.of());
        }
    }

    @Schema(description = "Appends a turn to the store without invoking the model. Intended for tests.")
    public record AppendTurnRequest(
            @Schema(description = "Who produced the turn.", example = "user",
                    allowableValues = {"user", "assistant"})
            @NotBlank @Pattern(regexp = "user|assistant", message = "role must be 'user' or 'assistant'")
            String role,
            @Schema(example = "Where is order 4711?")
            @NotBlank String content) {}
}
