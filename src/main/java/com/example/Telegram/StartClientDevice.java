package com.example.Telegram;

import com.example.Telegram.service.socket.ClientInitializer;

import java.io.IOException;
import java.net.InetAddress;


public class StartClientDevice {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Please provide userName and device info as arguments.");
            return;
        }

        String userName = args[0];
        String device_info = args[1];

        InetAddress inetAddress = InetAddress.getLocalHost();
        String ipAddress = inetAddress.getHostAddress();

        // Set up connect to server
        ClientInitializer clientInitializer = new ClientInitializer(userName, device_info, ipAddress);
        clientInitializer.initiateConnectionToServer();
    }
}

