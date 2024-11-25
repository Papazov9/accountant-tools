package com.wildrep.accountantapp.controller;

import com.wildrep.accountantapp.model.dto.ActivationCodeRequest;
import com.wildrep.accountantapp.model.dto.ActivationCodeResponse;
import com.wildrep.accountantapp.model.dto.ValidationCodeRequest;
import com.wildrep.accountantapp.service.AdminService;
import com.wildrep.accountantapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final PaymentService paymentService;

    @PostMapping("/activationCode")
    public ResponseEntity<ActivationCodeResponse> sendActivationCode(@RequestBody ActivationCodeRequest request) {

        try {
            this.adminService.generateAdminVerificationCode(request);
        } catch (RuntimeException e) {
            log.error("Invalid action or no permission to this endpoint! Stack trace: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ActivationCodeResponse("Invalid response", request.username(), false));
        }

        return ResponseEntity.ok(new ActivationCodeResponse("Successfully generated!", request.username(), true));
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<ActivationCodeResponse> validateActivationCode(@RequestBody ValidationCodeRequest validationCodeRequest) {
        try {
            this.adminService.validateCode(validationCodeRequest);
        } catch (RuntimeException e) {
            log.error("Invalid code or no permission to this endpoint! Stack trace: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ActivationCodeResponse("Invalid response", validationCodeRequest.username(), false));
        }

        return ResponseEntity.ok(new ActivationCodeResponse("Successfully generated!", validationCodeRequest.username(), true));
    }

    @GetMapping("/pending-payments")
    public ResponseEntity<List<PendingPayment>> loadPendingPayments() {
        try {
            List<PendingPayment> result = this.paymentService.loadPendingPayments();
            return ResponseEntity.ok(result);
        }catch (RuntimeException e) {
            log.error("Invalid action or no permission to this endpoint! Stack trace: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }
}
