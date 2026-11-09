package com.Auth.Exceptions.BuyerException;

public class RequiredThingsNotFoundException extends RuntimeException{

    public RequiredThingsNotFoundException(String msg) {
        super(msg);
    }
}
