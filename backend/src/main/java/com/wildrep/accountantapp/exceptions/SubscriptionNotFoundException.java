package com.wildrep.accountantapp.exceptions;

public class SubscriptionNotFoundException extends RuntimeException {
    private String message;

    public SubscriptionNotFoundException(String message) {

        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
