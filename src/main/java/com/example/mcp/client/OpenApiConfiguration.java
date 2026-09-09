package com.example.mcp.client;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI mcpAiClientOpenApi(@Value("${spring.application.name}") String applicationName) {
        return new OpenAPI()
                .info(new Info()
                        .title("MCP AI Client API")
                        .version("1.0.0")
                        .description("""
                            Enterprise order assistant built on Spring AI (Google Gemini) and the
                            Model Context Protocol.

                            Every endpoint is tenant-scoped: `X-Tenant-Id` and `X-User-Id` are required
                            request headers, and conversations are isolated per tenant/user/conversation
                            triple.

                            The endpoints under **conversations** read and write the conversation store
                            directly without calling the language model, so they can be exercised in tests
                            and local runs with no Gemini API key and no MCP server attached.
                            """)
                        .license(new License().name("Apache-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description(applicationName + " (local)")));
    }
}
