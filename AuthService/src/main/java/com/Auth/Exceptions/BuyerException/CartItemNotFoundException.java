package com.Auth.Exceptions.BuyerException;

public class CartItemNotFoundException extends RuntimeException{

    public CartItemNotFoundException(String msg) {
        super(msg);
    }
}
