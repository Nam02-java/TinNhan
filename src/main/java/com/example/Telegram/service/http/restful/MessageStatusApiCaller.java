package com.example.Telegram.service.http.restful;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.System.out;

public class MessageStatusApiCaller {

    private String SERVER_URL_MESSAGES;


    public MessageStatusApiCaller(String SERVER_URL_MESSAGES) {
        this.SERVER_URL_MESSAGES = SERVER_URL_MESSAGES;
    }

    public void setMessageStatusToSeen(Long messageId) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();

            // Create HttpRequest to call API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL_MESSAGES + "/seen?messageId=" + messageId))
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody()) // No body for POST request
                    .build();

            // Create HttpResponse to get the response from server
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // out.println("Set status for messages to seen successfully");
            } else {
                out.println("Error: HTTP response code " + response.statusCode());
            }
        } catch (Exception e) {
            out.println("Error during HTTP request: " + e.getMessage());
        }
    }
}
