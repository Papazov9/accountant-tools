package com.wildrep.accountantapp.exceptions;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String sessionId) {
        super("Payment not found for session id " + sessionId);
    }
}
