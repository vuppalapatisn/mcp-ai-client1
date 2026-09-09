package com.example.mcp.client;

import static com.example.mcp.client.ConversationModels.Turn;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ContextAssemblerTest {
    @Test
    void keepsTenantConversationsSeparate() {
        ConversationStore store = new ConversationStore();
        store.append("tenant-a", "user-1", "c1", new Turn("user", "A secret", Instant.now()));
        store.append("tenant-b", "user-1", "c1", new Turn("user", "B secret", Instant.now()));

        ContextAssembler assembler = new ContextAssembler(store);
        String context = assembler.assemble("tenant-a", "user-1", "c1", "hello").text();

        assertThat(context).contains("A secret").doesNotContain("B secret");
    }
}
