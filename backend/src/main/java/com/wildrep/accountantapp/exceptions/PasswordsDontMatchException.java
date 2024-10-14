package com.wildrep.accountantapp.exceptions;

public class PasswordsDontMatchException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Passwords do not match!";
    }
}
