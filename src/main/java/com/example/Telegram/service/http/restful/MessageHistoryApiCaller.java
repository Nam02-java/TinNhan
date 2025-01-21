package com.example.Telegram.service.http.restful;

import com.example.Telegram.service.json.JsonMessageParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class MessageHistoryApiCaller {

    private String SERVER_URL_MESSAGES;

    private JsonMessageParser jsonMessageParser;


    public MessageHistoryApiCaller(String SERVER_URL_MESSAGES, JsonMessageParser jsonMessageParser) {
        this.SERVER_URL_MESSAGES = SERVER_URL_MESSAGES;
        this.jsonMessageParser = jsonMessageParser;
    }


    public List<String> fetchChatHistoryFromServer(int currentPage) {
        String jsonResponse = "";
        List<String> historyMessages;
        try {
            HttpClient httpClient = HttpClient.newHttpClient();

            // Create HttpRequest to call api
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL_MESSAGES + "/history?currentPage=" + currentPage + "&pageSize=5"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            // Create HttpResponse to get data from API
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            /**
             * 200 OK is true !
             */
            if (response.statusCode() == 200) {
                // Get data from JSON
                jsonResponse = response.body();
            } else {
                out.println("Error: HTTP response code " + response.statusCode());
            }
        } catch (Exception e) {
            out.println("Error during HTTP request: " + e.getMessage());
        }

        // Parse the JSON to get a list of messages
        historyMessages = parseHistoryMessages(jsonResponse);
        return historyMessages;
    }

    private List<String> parseHistoryMessages(String jsonResponse) {
        /**
         * Actual returned json sample from database
         * {"sender":{"id":1,"username":"Linh02"},"recipient":{"id":2,"username":"Nam02"},"id":44,"sentAt":"2024-12-30T16:08:51","messageContent":"demo"}
         */
        List<String> messages = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray content = json.getJSONArray("content");

            for (int i = 0; i < content.length(); i++) {
                JSONObject messageJson = content.getJSONObject(i);

                jsonMessageParser.parseFromAPIServer(messageJson.toString());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedSentAt = jsonMessageParser.getSentAt().format(formatter);

                // Combine time, sender name and message content
                String combinedMessage = String.format("[%s] %s: %s", formattedSentAt, jsonMessageParser.getUsername(), jsonMessageParser.getMessage());
                messages.add(combinedMessage);
            }

        } catch (JSONException e) {
            out.println("Error parsing JSON response: " + e.getMessage());
        }

        return messages;

    }
}
