package com.wildrep.accountantapp.model.dto;

public record PaymentSessionRequest(String amount,
                                    String subscriptionType,
                                    String username) {
}
