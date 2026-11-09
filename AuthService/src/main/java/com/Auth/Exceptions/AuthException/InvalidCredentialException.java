package com.Auth.Exceptions.AuthException;

public class InvalidCredentialException extends RuntimeException{

    public InvalidCredentialException(String message) {
        super(message);
    }
}
