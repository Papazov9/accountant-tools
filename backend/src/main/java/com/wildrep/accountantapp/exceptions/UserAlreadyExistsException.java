package com.wildrep.accountantapp.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    private String username;

    public UserAlreadyExistsException(String username) {
        this.username = username;
    }

    @Override
    public String getMessage() {
        return String.format("User with username: %s already exists!", this.username);
    }
}
