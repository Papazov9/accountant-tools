package com.wildrep.accountantapp.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.wildrep.accountantapp.controller.PaymentStatusResponse;
import com.wildrep.accountantapp.exceptions.PaymentNotFoundException;
import com.wildrep.accountantapp.model.Payment;
import com.wildrep.accountantapp.model.User;
import com.wildrep.accountantapp.model.dto.PaymentSessionRequest;
import com.wildrep.accountantapp.model.enums.PaymentStatus;
import com.wildrep.accountantapp.model.enums.SubscriptionType;
import com.wildrep.accountantapp.repo.PaymentRepository;
import com.wildrep.accountantapp.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.webhook.secret}")
    private String endpointCheckoutCompletedSecret;

    @Value("${stripe.api.key}")
    private String apiKey;

    private static final String EVENT_TYPE_CHECKOUT_SESSION = "checkout.session.completed";

    private final UserService userService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    public Map<String, String> createCheckoutSession(PaymentSessionRequest request) throws StripeException {

        Long amount = Long.parseLong(request.amount());
        String subscriptionType = request.subscriptionType().toUpperCase();
        String username = request.username();
        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:4200/success?sessionId={CHECKOUT_SESSION_ID}")
                        .setCancelUrl("http://localhost:4200/cancel")
                        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                        .setCurrency("eur")
                        .putMetadata("username", username)
                        .putMetadata("subscriptionType", subscriptionType)
                        .addLineItem(SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("One-time payment")
                                                                .build())
                                                .build())
                                .build())
                        .build();
        Session session = Session.create(params);
        String sessionId = session.getId();

        Payment payment = Payment.builder()
                .user(user)
                .sessionId(sessionId)
                .amountInCents(amount)
                .subscriptionType(SubscriptionType.valueOf(subscriptionType.toUpperCase()))
                .build();
        this.paymentRepository.saveAndFlush(payment);

        Map<String, String> chargeParams = new HashMap<>();
        chargeParams.put("url", sessionId);
        return chargeParams;
    }

    public Map<String, String> handleSuccessfulPayment(String payload, String signatureHeader) {
        Map<String, String> body = new HashMap<>();
        Payment payment = null;
        try {
            Event event = Webhook.constructEvent(payload, signatureHeader, endpointCheckoutCompletedSecret);

            if (EVENT_TYPE_CHECKOUT_SESSION.equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    String username = session.getMetadata().get("username");
                    String subscriptionType = session.getMetadata().get("subscriptionType");

                    String result = this.userService.updateUserSubscription(subscriptionType, username);
                    body.put("message", result);
                    body.put("status", "success");
                    String sessionId = session.getId();
                    payment = this.paymentRepository
                            .findPaymentBySessionId(sessionId)
                            .orElseThrow(() -> new PaymentNotFoundException(sessionId));
                    payment.setStatus(PaymentStatus.SUCCEEDED);
                    this.paymentRepository.saveAndFlush(payment);
                }
            }
        } catch (SignatureVerificationException e) {
            if (payment != null) {
                payment.setStatus(PaymentStatus.FAILED);
                this.paymentRepository.saveAndFlush(payment);
            }
            throw new RuntimeException(e);
        }
        body.put("status", "failed");
        return body;
    }

    public PaymentStatusResponse getPaymentStatus(String sessionId) {
        Payment payment = this.paymentRepository.findPaymentBySessionId(sessionId).orElseThrow(() -> new PaymentNotFoundException(sessionId));

        return new PaymentStatusResponse(payment.getStatus().name().toLowerCase(),"Successful!");
    }
}
