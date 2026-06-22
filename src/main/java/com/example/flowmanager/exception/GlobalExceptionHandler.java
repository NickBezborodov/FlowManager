package com.example.flowmanager.exception;

public class GlobalExceptionHandler extends RuntimeException {
    public GlobalExceptionHandler(String message, Throwable cause){
        super(message, cause);
    }
}
