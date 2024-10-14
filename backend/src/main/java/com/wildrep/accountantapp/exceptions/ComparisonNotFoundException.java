package com.wildrep.accountantapp.exceptions;

public class ComparisonNotFoundException extends RuntimeException{
    private String batchId;

    public ComparisonNotFoundException(String batchId) {
        this.batchId = batchId;
    }

    @Override
    public String getMessage() {
        return String.format("Comparison with this batchId: %s does not exists!", batchId);
    }
}
