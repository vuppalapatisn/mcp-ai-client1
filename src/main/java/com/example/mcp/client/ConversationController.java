package com.example.mcp.client;

import static com.example.mcp.client.ConversationModels.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read/write access to the conversation store that bypasses the language model.
 *
 * <p>Useful for exercising context assembly and tenant isolation without an OpenAI
 * key or an attached MCP server. Because these endpoints let a caller write
 * arbitrary conversation history, they can be turned off with
 * {@code app.testing-api.enabled=false}.
 */
@RestController
@RequestMapping("/api/conversations")
@ConditionalOnProperty(name = "app.testing-api.enabled", havingValue = "true", matchIfMissing = true)
@Tag(name = "conversations",
        description = "Inspect and seed conversation state directly. No model call, no MCP server needed.")
public class ConversationController {
    private final ConversationStore store;
    private final ContextAssembler contextAssembler;

    public ConversationController(ConversationStore store, ContextAssembler contextAssembler) {
        this.store = store;
        this.contextAssembler = contextAssembler;
    }

    @GetMapping("/{conversationId}/turns")
    @Operation(summary = "List the turns recorded for a conversation, oldest first.")
    public List<Turn> turns(
            @Parameter(description = "Tenant the conversation belongs to.", example = "tenant-a")
            @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "User within the tenant.", example = "user-1")
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String conversationId) {
        return store.turns(tenantId, userId, conversationId);
    }

    @PostMapping("/{conversationId}/turns")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Append a turn to a conversation without invoking the model.",
            description = "Lets a test build up history, then check what assemble() selects from it.")
    @ApiResponse(responseCode = "201", description = "Turn appended; the full history is returned.")
    public List<Turn> appendTurn(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String conversationId,
            @Valid @RequestBody AppendTurnRequest request) {
        store.append(tenantId, userId, conversationId,
                new Turn(request.role(), request.content(), Instant.now()));
        return store.turns(tenantId, userId, conversationId);
    }

    @GetMapping("/{conversationId}/context")
    @Operation(summary = "Preview the prompt context that would be sent for a message.",
            description = """
                Runs the same assembly the chat endpoint uses — summary, then the newest turns
                that fit the 12,000 character budget, then the incoming message — and returns it
                instead of sending it to the model.
                """)
    public ContextAssembler.AssembledContext context(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String conversationId,
            @Parameter(description = "The message that would be appended to the context.",
                    example = "Where is order 4711?")
            @RequestParam(defaultValue = "") String message) {
        return contextAssembler.assemble(tenantId, userId, conversationId, message);
    }

    @GetMapping("/{conversationId}/summary")
    @Operation(summary = "Read the running summary, or an empty summary if none was stored.")
    public ConversationSummary summary(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String conversationId) {
        return store.summary(tenantId, userId, conversationId);
    }

    @PutMapping("/{conversationId}/summary")
    @Operation(summary = "Replace the running summary for a conversation.")
    public ConversationSummary saveSummary(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String conversationId,
            @RequestBody ConversationSummary summary) {
        store.saveSummary(tenantId, userId, conversationId, summary);
        return store.summary(tenantId, userId, conversationId);
    }
}
