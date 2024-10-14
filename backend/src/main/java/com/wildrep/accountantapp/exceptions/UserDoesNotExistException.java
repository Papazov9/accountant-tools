package com.wildrep.accountantapp.exceptions;

public class UserDoesNotExistException extends RuntimeException {

    private String username;

    public UserDoesNotExistException(String username) {
    }

    @Override
    public String getMessage() {
        return String.format("User with username: %s does not exist!", this.username);
    }
}
