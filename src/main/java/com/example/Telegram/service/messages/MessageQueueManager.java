package com.example.Telegram.service.messages;

import org.json.JSONObject;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueueManager {
    private final Queue<JSONObject> unsentMessages;

    public MessageQueueManager() {
        this.unsentMessages = new ConcurrentLinkedQueue<>();
    }

    public void addMessage(JSONObject message) {
        unsentMessages.offer(message);
    }

    public JSONObject getNextMessage() {
        return unsentMessages.poll();
    }

    public boolean hasPendingMessages() {
        return !unsentMessages.isEmpty();
    }
}
