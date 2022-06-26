package com.example.exception;

import java.io.FileNotFoundException;

public class ApiRequestException extends Exception {
    public ApiRequestException(String message) {
        super(message);
    }

    public ApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
