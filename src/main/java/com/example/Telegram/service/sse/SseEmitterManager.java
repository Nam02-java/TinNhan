package com.example.Telegram.service.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void addEmitter(String sessionToken, SseEmitter emitter) {
        if (sessionToken == null || emitter == null) {
            return;
        }

        SseEmitter existingEmitter = emitters.get(sessionToken);
        if (existingEmitter != null) {
            existingEmitter.complete();
        }
        emitters.put(sessionToken, emitter);
        System.out.println("add emitter from sseEmitterManage.class " + sessionToken);
    }

    public synchronized void removeEmitter(String sessionKey) {
        SseEmitter emitter = emitters.remove(sessionKey);
        if (emitter != null) {
            System.out.println("remove emitter from sseEmitterManage.class");
            emitter.complete();
        }
    }

    public synchronized void completeAllEmitters() {
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            SseEmitter emitter = entry.getValue();
            if (emitter != null) {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    System.err.println("Error completing emitter: " + e.getMessage());
                }
            }
        }
        emitters.clear();
        System.out.println("All SSE Emitters completed.");
    }


    public synchronized void printAllEmitters() {
        System.out.println("Currently stored SSE Emitters:");
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            String sessionKey = entry.getKey();
            SseEmitter emitter = entry.getValue();
            System.out.println("Username: " + sessionKey + ", Emitter: " + emitter);
        }
    }
}
