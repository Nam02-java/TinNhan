package com.example.Telegram.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionRequestDTO {
    private String username;
    private String deviceInfo;
    private String ipAddress;

    private String socket; //Add the socket_info field to the SessionRequestDTO

}
