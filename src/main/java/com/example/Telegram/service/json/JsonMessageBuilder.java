package com.example.Telegram.service.json;

import org.json.JSONObject;

import java.time.LocalDateTime;

public class JsonMessageBuilder {

    private JSONObject jsonMessage;

    public JsonMessageBuilder() {
        this.jsonMessage = new JSONObject();
    }

    public JSONObject build(String username, String message) {
        jsonMessage.clear();
        jsonMessage.put("username", username);
        jsonMessage.put("message", message);
        jsonMessage.put("sentAt", LocalDateTime.now().toString());
        return jsonMessage;
    }

    public JSONObject getJsonMessage() {
        return jsonMessage;
    }
}
