package com.example.Telegram.exception;

import java.io.IOException;

public class ConnectionLostException extends IOException {
    public ConnectionLostException(String message) {
        super(message);
    }

    public ConnectionLostException(String message, Throwable cause) {
        super(message, cause);
    }
}
