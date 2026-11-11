package com.Order.exception;

public class ProductIsNotAvailable extends RuntimeException{

    public ProductIsNotAvailable(String msg) {
        super(msg);
    }
}
