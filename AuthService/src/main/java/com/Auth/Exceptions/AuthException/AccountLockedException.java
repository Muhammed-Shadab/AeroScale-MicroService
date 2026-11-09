package com.Auth.Exceptions.AuthException;

public class AccountLockedException extends RuntimeException{

    public AccountLockedException(String msg) {
        super(msg);
    }
}
