package com.wildrep.accountantapp.model.dto;

public record PaymentSessionRequest(String contactEmail,
                                    String companyName,
                                    String addressLine1,
                                    String addressLine2,
                                    String city,
                                    String postalCode,
                                    String country,
                                    Long price,
                                    String subscriptionType) {
}
