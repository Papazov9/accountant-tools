package com.wildrep.accountantapp.exceptions;

public class NoComparisonsLeftException extends RuntimeException {

    public NoComparisonsLeftException() {}

    @Override
    public String getMessage() {
        return "No more comparisons left!";
    }
}
