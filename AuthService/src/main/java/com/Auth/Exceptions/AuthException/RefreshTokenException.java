package com.Auth.Exceptions.AuthException;

public class RefreshTokenException extends RuntimeException{

    public RefreshTokenException(String msg) {
        super(msg);
    }
}
