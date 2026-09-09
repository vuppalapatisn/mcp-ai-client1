package com.example.mcp.client;

import static com.example.mcp.client.ConversationModels.*;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ConversationService service;

    public ChatController(ConversationService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ChatResponse> chat(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ChatRequest request) {
        return service.chat(tenantId, userId, request);
    }
}
