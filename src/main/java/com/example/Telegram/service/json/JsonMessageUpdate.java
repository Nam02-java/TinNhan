package com.example.Telegram.service.json;

import org.json.JSONObject;

public class JsonMessageUpdate {

    /**
     * Adds a new field to a JSON string.
     *
     * @param jsonString The original JSON string.
     * @param fieldName  The name of the field to add.
     * @param value      The value of the field to add.
     * @return A new JSON string with the field added.
     */
    public  String addField(String jsonString, String fieldName, Object value) {
        JSONObject jsonObject = new JSONObject(jsonString);
        jsonObject.put(fieldName, value);
        return jsonObject.toString();
    }
}
