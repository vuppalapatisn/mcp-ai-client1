package com.example.mcp.client;

import static com.example.mcp.client.ConversationModels.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ContextAssembler {
    private static final int MAX_HISTORY_CHARACTERS = 12_000;
    private final ConversationStore store;

    public ContextAssembler(ConversationStore store) {
        this.store = store;
    }

    public AssembledContext assemble(
            String tenantId, String userId, String conversationId, String currentMessage) {

        ConversationSummary summary = store.summary(tenantId, userId, conversationId);
        List<Turn> selected = newestTurnsWithinBudget(
                store.turns(tenantId, userId, conversationId), MAX_HISTORY_CHARACTERS);

        StringBuilder text = new StringBuilder();
        if (!summary.objective().isBlank()) {
            text.append("Conversation summary:\nObjective: ").append(summary.objective()).append("\n");
            text.append("Verified facts: ").append(summary.verifiedFacts()).append("\n");
            text.append("Pending questions: ").append(summary.pendingQuestions()).append("\n\n");
        }
        text.append("Recent conversation:\n");
        selected.forEach(turn -> text.append(turn.role()).append(": ").append(turn.content()).append("\n"));
        text.append("user: ").append(currentMessage);
        return new AssembledContext(text.toString(), text.length());
    }

    private List<Turn> newestTurnsWithinBudget(List<Turn> turns, int budget) {
        List<Turn> chosen = new ArrayList<>();
        int used = 0;
        for (int i = turns.size() - 1; i >= 0; i--) {
            Turn turn = turns.get(i);
            int size = turn.content().length();
            if (used + size > budget) break;
            chosen.add(turn);
            used += size;
        }
        Collections.reverse(chosen);
        return chosen;
    }

    public record AssembledContext(String text, int characterCount) {}
}
