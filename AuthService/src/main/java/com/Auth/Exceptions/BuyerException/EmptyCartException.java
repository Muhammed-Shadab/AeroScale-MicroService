package com.Auth.Exceptions.BuyerException;

public class EmptyCartException extends RuntimeException{

    public EmptyCartException(String msg) {
        super(msg);
    }
}
