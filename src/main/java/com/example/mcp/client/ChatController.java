package com.example.mcp.client;

import static com.example.mcp.client.ConversationModels.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "chat", description = "Ask the assistant a question. Calls the language model and MCP tools.")
public class ChatController {
    private final ConversationService service;

    public ChatController(ConversationService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Answer a message in the context of a conversation.",
            description = """
                Assembles prompt context from the stored conversation, calls the model with the
                MCP tool callbacks attached, then records both the user message and the answer.

                Requires a usable `OPENAI_API_KEY` and a reachable MCP server. To exercise the
                surrounding logic without either, use the **conversations** endpoints.
                """)
    public Mono<ChatResponse> chat(
            @Parameter(description = "Tenant the conversation belongs to.", example = "tenant-a")
            @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "User within the tenant.", example = "user-1")
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ChatRequest request) {
        return service.chat(tenantId, userId, request);
    }
}
