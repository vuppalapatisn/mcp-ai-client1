package com.example.mcp.client;

import static com.example.mcp.client.ConversationModels.*;

import java.time.Instant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConversationService {
    private final ChatClient chatClient;
    private final ConversationStore store;
    private final ContextAssembler contextAssembler;

    public ConversationService(
            ChatClient chatClient,
            ConversationStore store,
            ContextAssembler contextAssembler) {
        this.chatClient = chatClient;
        this.store = store;
        this.contextAssembler = contextAssembler;
    }

    public Mono<ChatResponse> chat(
            String tenantId, String userId, ChatRequest request) {

        ContextAssembler.AssembledContext context = contextAssembler.assemble(
                tenantId, userId, request.conversationId(), request.message());

        return Mono.fromCallable(() -> chatClient.prompt()
                        .user(context.text())
                        .call()
                        .content())
                .map(answer -> {
                    store.append(tenantId, userId, request.conversationId(),
                            new Turn("user", request.message(), Instant.now()));
                    store.append(tenantId, userId, request.conversationId(),
                            new Turn("assistant", answer, Instant.now()));
                    return new ChatResponse(
                            request.conversationId(), answer, context.characterCount(), Instant.now());
                });
    }
}
