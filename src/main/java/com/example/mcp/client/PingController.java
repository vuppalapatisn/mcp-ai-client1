package com.example.mcp.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
@Tag(name = "diagnostics", description = "Unauthenticated liveness check for smoke tests.")
public class PingController {
    private final String applicationName;

    public PingController(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping
    @Operation(summary = "Confirm the service is up.",
            description = "Takes no headers and touches no dependencies, so it answers even when "
                    + "no MCP server is attached and no OpenAI key is configured.")
    public Pong ping() {
        return new Pong(applicationName, "UP", Instant.now());
    }

    @Schema(description = "Liveness response.")
    public record Pong(
            @Schema(example = "mcp-ai-client") String application,
            @Schema(example = "UP") String status,
            Instant timestamp) {}
}
