package com.example.mcp.client;

import static com.example.mcp.client.ConversationModels.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ConversationStore {
    private final Map<String, List<Turn>> turns = new ConcurrentHashMap<>();
    private final Map<String, ConversationSummary> summaries = new ConcurrentHashMap<>();

    public List<Turn> turns(String tenantId, String userId, String conversationId) {
        return List.copyOf(turns.computeIfAbsent(key(tenantId, userId, conversationId), ignored -> new ArrayList<>()));
    }

    public synchronized void append(String tenantId, String userId, String conversationId, Turn turn) {
        turns.computeIfAbsent(key(tenantId, userId, conversationId), ignored -> new ArrayList<>()).add(turn);
    }

    public ConversationSummary summary(String tenantId, String userId, String conversationId) {
        return summaries.getOrDefault(key(tenantId, userId, conversationId), ConversationSummary.empty());
    }

    public void saveSummary(String tenantId, String userId, String conversationId, ConversationSummary summary) {
        summaries.put(key(tenantId, userId, conversationId), summary);
    }

    private static String key(String tenantId, String userId, String conversationId) {
        return tenantId + ":" + userId + ":" + conversationId;
    }
}
