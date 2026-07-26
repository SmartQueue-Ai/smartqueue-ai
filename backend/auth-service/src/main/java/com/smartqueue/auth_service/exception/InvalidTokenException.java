package com.smartqueue.auth_service.exception;

public class InvalidTokenException extends AuthException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
