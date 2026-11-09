package com.Auth.Exceptions.BuyerException;

public class InsufficientStockException extends RuntimeException{
    public InsufficientStockException(String msg) {
        super(msg);

    }
}
