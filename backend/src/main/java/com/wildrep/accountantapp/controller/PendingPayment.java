package com.wildrep.accountantapp.controller;

public record PendingPayment(String invoiceNumber,
                             String status,
                             String googleDriveLink,
                             String customer) {
}
