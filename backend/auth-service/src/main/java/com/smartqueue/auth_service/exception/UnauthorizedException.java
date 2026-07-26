package com.smartqueue.auth_service.exception;

public class UnauthorizedException extends AuthException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
