package com.wildrep.accountantapp.controller;

import com.stripe.exception.StripeException;
import com.wildrep.accountantapp.model.dto.PaymentSessionRequest;
import com.wildrep.accountantapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody PaymentSessionRequest paymentSessionRequest) {
        try {
            Map<String, String> url = paymentService.handlePaymentRequest(paymentSessionRequest);
            return ResponseEntity.ok(url);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("status", "failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

//    @GetMapping("/payment-status")
//    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@RequestParam String sessionId) {
//        try {
//            PaymentStatusResponse paymentStatusResponse = this.paymentService.getPaymentStatus(sessionId);
//            return ResponseEntity.ok(paymentStatusResponse);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PaymentStatusResponse(null, "Failed"));
//        }
//    }
}
