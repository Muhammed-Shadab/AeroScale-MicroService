package com.inventory.exception;

public class ProductIsNotAvailable extends RuntimeException {
    public ProductIsNotAvailable(String message) {
        super(message);
    }
}