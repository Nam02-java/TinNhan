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

import static java.lang.System.out;

public class MessageUnreadApiCaller {
    private String SERVER_URL_MESSAGES;
    private JsonMessageParser jsonMessageParser;


    public MessageUnreadApiCaller(String SERVER_URL_MESSAGES, JsonMessageParser jsonMessageParser) {
        this.SERVER_URL_MESSAGES = SERVER_URL_MESSAGES;
        this.jsonMessageParser = jsonMessageParser;
    }

    public void fetchUnreadMessagesFromServer(String username) {
        String jsonResponse = "";
        try {
            HttpClient httpClient = HttpClient.newHttpClient();

            // Create HttpRequest to call API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL_MESSAGES + "/downloadUnreadMessages"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(username)) // add name to body
                    .build();

            // Create HttpResponse to get the response from server
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                jsonResponse = response.body();
            } else {
                out.println("Error: HTTP response code " + response.statusCode());
                return;
            }


            try {
                JSONArray messages = new JSONArray(jsonResponse);

                out.println("-----------------unread messages-----------------");
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject messageJson = messages.getJSONObject(i);

                    jsonMessageParser.parseFromAPIServer(messageJson.toString());  // Parse information from API

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedSentAt = jsonMessageParser.getSentAt().format(formatter);

                    out.println(String.format("[%s] %s: %s", formattedSentAt, jsonMessageParser.getUsername(), jsonMessageParser.getMessage()));
                }
                out.println("-----------------unread messages-----------------");
            } catch (JSONException e) {
                out.println("Error parsing JSON response: " + e.getMessage());
            }


        } catch (Exception e) {
            out.println("Error during HTTP request: " + e.getMessage());
        }
    }
}
