package com.example.Telegram.service.json;

import org.json.JSONObject;

import java.time.LocalDateTime;

public class JsonMessageParser {
    private Long id;
    private String username;
    private String message;
    private LocalDateTime sentAt;
    public void parseJson(String jsonString) {
        JSONObject jsonMessage = new JSONObject(jsonString);
        if (jsonMessage.has("id")) {
            this.id = jsonMessage.getLong("id");
        }
        this.username = jsonMessage.getString("username");
        this.message = jsonMessage.getString("message");

        String sentAtString = jsonMessage.getString("sentAt");
        this.sentAt = LocalDateTime.parse(sentAtString); // Assuming ISO 8601 format
    }

    public void parseFromAPIServer(String jsonString) {
        JSONObject jsonMessage = new JSONObject(jsonString);
        JSONObject senderJson = jsonMessage.getJSONObject("sender");

        this.username = senderJson.getString("username");
        this.message = jsonMessage.getString("messageContent");

        String sentAtString = jsonMessage.getString("sentAt");
        this.sentAt = LocalDateTime.parse(sentAtString);
    }


    // Getters


    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
