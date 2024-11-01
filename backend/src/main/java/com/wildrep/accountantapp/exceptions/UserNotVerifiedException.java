package com.wildrep.accountantapp.exceptions;

public class UserNotVerifiedException extends RuntimeException {

    private final String message;

    public UserNotVerifiedException(String username) {
        this.message = "User " + username + " is not verified!";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
