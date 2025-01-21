package com.example.Telegram.service.http.sse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class NotificationApiCaller {

    private String SERVER_URL = "http://localhost:8080/api/notifications";

    public void listeningNotifyMessageSeen(String currentUsername) {
        try {
            // Create the request URL
            String url = SERVER_URL + "/" + currentUsername + "/listening-message-seen";

            // Open connection to server
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream"); // SSE-specific header

            if (connection.getResponseCode() == 200) {
                System.out.println("Connected to SSE stream for user: " + currentUsername);

                // Process SSE stream
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isEmpty()) {
                            System.out.println("Received SSE " + line);
                        }
                    }
                }
            } else {
                System.err.println("Failed to connect to SSE: HTTP " + connection.getResponseCode());
            }
        } catch (IOException e) {
            System.err.println("Error during SSE connection: " + e.getMessage());
        }
    }
}
