package com.wildrep.accountantapp.model.dto;

public record SubscriptionResponse(String title,
                                   Double price,
                                   Integer comparisons,
                                   String pros) {
}
