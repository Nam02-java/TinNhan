package com.example.Telegram.service.http.restful;

import com.example.Telegram.model.dto.SessionRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UserSessionApiCaller {

    private String SERVER_URL ;

    public UserSessionApiCaller(String SERVER_URL) {
        this.SERVER_URL = SERVER_URL;
    }

    public void createSession(String username, String deviceInfo, String ipAddress, String socket) {
        try {
            // Initialize DTO and convert to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            SessionRequestDTO sessionRequest = new SessionRequestDTO(username, deviceInfo, ipAddress, socket);
            String jsonBody = objectMapper.writeValueAsString(sessionRequest);

            // Create request with data
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            /**
             * The client does not need a response
             */
            // Create response to get data from API
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Session created successfully: " + response.body());
            } else {
                System.err.println("Error: HTTP response code " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error during HTTP request: " + e.getMessage());
        }
    }
}

