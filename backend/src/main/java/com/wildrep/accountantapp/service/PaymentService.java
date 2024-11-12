package com.wildrep.accountantapp.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import com.wildrep.accountantapp.controller.PaymentStatusResponse;
import com.wildrep.accountantapp.exceptions.PaymentNotFoundException;
import com.wildrep.accountantapp.exceptions.SubscriptionNotFoundException;
import com.wildrep.accountantapp.exceptions.UserDoesNotExistException;
import com.wildrep.accountantapp.model.Payment;
import com.wildrep.accountantapp.model.Subscription;
import com.wildrep.accountantapp.model.User;
import com.wildrep.accountantapp.model.dto.PaymentSessionRequest;
import com.wildrep.accountantapp.model.enums.PaymentStatus;
import com.wildrep.accountantapp.model.enums.SubscriptionType;
import com.wildrep.accountantapp.repo.PaymentRepository;
import com.wildrep.accountantapp.repo.SubscriptionRepository;
import com.wildrep.accountantapp.repo.UserRepository;
import com.wildrep.accountantapp.util.PDFWriterUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.RequestContextFilter;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RequestContextFilter requestContextFilter;
    @Value("${stripe.webhook.secret}")
    private String endpointCheckoutCompletedSecret;

    @Value("${stripe.api.key}")
    private String apiKey;

    private static final String PAYMENT_METHOD_CARD = "card";
    private static final String PAYMENT_METHOD_BANK_TRANSFER = "bank";

    private static final String EVENT_TYPE_CHECKOUT_SESSION = "checkout.session.completed";
    private static final String EVENT_TYPE_PAYMENT_INTENT = "payment_intent.succeeded";

    private final UserService userService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;
    private final SubscriptionRepository subscriptionRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    public Map<String, String> handlePaymentRequest(PaymentSessionRequest paymentSessionRequest) throws StripeException {
        User user = this.userRepository.findByEmail(paymentSessionRequest.contactEmail()).orElseThrow(() -> new UserDoesNotExistException(paymentSessionRequest.contactEmail()));
        String stripeCustomerId = user.getStripeCustomerId();
        if (stripeCustomerId == null) {
            stripeCustomerId = createStripeCustomerId(user, paymentSessionRequest);
        }
//        if (PAYMENT_METHOD_CARD.equals(paymentSessionRequest.paymentMethod())) {
//            return createCardCheckoutSession(paymentSessionRequest, user);
//        } else if (PAYMENT_METHOD_BANK_TRANSFER.equals(paymentSessionRequest.paymentMethod())) {
//            return createProFormaPaymentIntent(paymentSessionRequest, stripeCustomerId, user);
//        }
        return createInvoiceForPayment(paymentSessionRequest, stripeCustomerId, user);
    }

    private Map<String, String> createInvoiceForPayment(PaymentSessionRequest paymentSessionRequest, String stripeCustomerId, User user) {
        Map<String, String> result = new HashMap<>();

        Payment payment = Payment.builder()
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .amountInCents(paymentSessionRequest.price())
                .user(user)
                .build();
        Subscription subscription = this.subscriptionRepository
                .findByType(SubscriptionType.valueOf(paymentSessionRequest.subscriptionType().toUpperCase()))
                .orElseThrow(() ->
                        new SubscriptionNotFoundException("Subscription from type: "
                                + paymentSessionRequest.subscriptionType()
                                + " was not found!"));

        InvoiceCreateParams invoiceParams =
                InvoiceCreateParams.builder()
                        .setCustomer(stripeCustomerId)
                        .setCollectionMethod(InvoiceCreateParams.CollectionMethod.SEND_INVOICE)
                        .putMetadata("subscriptionType", subscription.getType().name())
                        .putMetadata("locale", "bg")
                        .setPaymentSettings(InvoiceCreateParams.PaymentSettings.builder()
                                .addPaymentMethodType(InvoiceCreateParams.PaymentSettings.PaymentMethodType.CARD)
                                .addPaymentMethodType(InvoiceCreateParams.PaymentSettings.PaymentMethodType.CUSTOMER_BALANCE).build())
                        .setCurrency("eur")
                        .setDaysUntilDue(30L)
                        .build();
        Invoice invoice;
        try {
            invoice = Invoice.create(invoiceParams);
        } catch (StripeException e) {
            payment.setStatus(PaymentStatus.FAILED);
            this.paymentRepository.saveAndFlush(payment);
            result.put("status", "failed");
            log.error(e.getMessage());
            return result;
        }

        String priceId = "";

        try {
            Product product = Product.retrieve(subscription.getStripeProductId());
            priceId = product.getDefaultPrice();
        } catch (StripeException e) {
            payment.setStatus(PaymentStatus.FAILED);
            this.paymentRepository.saveAndFlush(payment);
            result.put("status", "failed");
            log.error(e.getMessage());
            return result;
        }

        InvoiceItemCreateParams invoiceItemParams =
                InvoiceItemCreateParams.builder()
                        .setCustomer(stripeCustomerId)
                        .setPrice(priceId)
                        .setCurrency("eur")
                        .setInvoice(invoice.getId())
                        .build();

        try {

            InvoiceItem.create(invoiceItemParams);
        } catch (StripeException e) {
            payment.setStatus(PaymentStatus.FAILED);
            this.paymentRepository.saveAndFlush(payment);
            result.put("status", "failed");
            log.error(e.getMessage());
            return result;
        }

        InvoiceSendInvoiceParams params = InvoiceSendInvoiceParams.builder().build();

        try {

            invoice.sendInvoice(params);
        } catch (StripeException e) {
            payment.setStatus(PaymentStatus.FAILED);
            this.paymentRepository.saveAndFlush(payment);
            result.put("status", "failed");
            log.error(e.getMessage());
            return result;
        }


        payment.setSubscriptionType(subscription.getType());
        payment.setInvoiceId(invoice.getId());

        this.paymentRepository.saveAndFlush(payment);
        result.put("status", "success");
        result.put("email", user.getEmail());
        log.info("Payment successfully created! Invoice id: {}", invoice.getId());
        return result;
    }

    private String createStripeCustomerId(User user, PaymentSessionRequest paymentSessionRequest) throws StripeException {
        CustomerCreateParams params =
                CustomerCreateParams.builder()
                        .setName(paymentSessionRequest.companyName() != null && !paymentSessionRequest.companyName().trim().isBlank()
                                ? paymentSessionRequest.companyName()
                                : user.getFirstName() + " " + user.getLastName())
                        .setEmail(user.getEmail())
                        .setAddress(CustomerCreateParams.Address.builder()
                                .setCountry(paymentSessionRequest.country())
                                .setCity(paymentSessionRequest.city())
                                .setPostalCode(paymentSessionRequest.postalCode())
                                .setLine1(paymentSessionRequest.addressLine1())
                                .setLine2(paymentSessionRequest.addressLine2())
                                .build())
                        .setShipping(CustomerCreateParams.Shipping.builder()
                                .setAddress(CustomerCreateParams.Shipping.Address.builder()
                                        .setCountry(paymentSessionRequest.country())
                                        .setCity(paymentSessionRequest.city())
                                        .setPostalCode(paymentSessionRequest.postalCode())
                                        .setLine1(paymentSessionRequest.addressLine1())
                                        .setLine2(paymentSessionRequest.addressLine2())
                                        .build())
                                .build())
                        .build();

        Customer customer = Customer.create(params);
        user.setStripeCustomerId(customer.getId());
        this.userRepository.save(user);
        return customer.getId();
    }

    private Map<String, String> createProFormaPaymentIntent(PaymentSessionRequest paymentSessionRequest, String stripeCustomerId, User user) throws StripeException {

        PaymentIntentCreateParams params = PaymentIntentCreateParams
                .builder()
                .setAmount(paymentSessionRequest.price())
                .setCustomer(stripeCustomerId)
                .setCurrency("eur")
                .putExtraParam("payment_method_types[0]", "customer_balance")
                .putMetadata("subscriptionType", paymentSessionRequest.subscriptionType())
                .setPaymentMethodOptions(
                        PaymentIntentCreateParams.PaymentMethodOptions.builder()
                                .setCustomerBalance(
                                        PaymentIntentCreateParams.PaymentMethodOptions.CustomerBalance.builder()
                                                .setFundingType(
                                                        PaymentIntentCreateParams
                                                                .PaymentMethodOptions
                                                                .CustomerBalance
                                                                .FundingType.BANK_TRANSFER
                                                )
                                                .setBankTransfer(PaymentIntentCreateParams
                                                        .PaymentMethodOptions
                                                        .CustomerBalance
                                                        .BankTransfer.builder()
                                                        .setType(PaymentIntentCreateParams
                                                                .PaymentMethodOptions
                                                                .CustomerBalance
                                                                .BankTransfer
                                                                .Type.EU_BANK_TRANSFER)
                                                        .setEuBankTransfer(
                                                                PaymentIntentCreateParams
                                                                        .PaymentMethodOptions
                                                                        .CustomerBalance
                                                                        .BankTransfer
                                                                        .EuBankTransfer.builder()
                                                                        .setCountry("NL")
                                                                        .build()
                                                        ).build())
                                                .build()
                                ).build()
                )
                .setReturnUrl("http://localhost:4200/payment-pending")
                .setPaymentMethodData(PaymentIntentCreateParams.PaymentMethodData.builder()
                        .setType(PaymentIntentCreateParams.PaymentMethodData.Type.CUSTOMER_BALANCE)
                        .build())
                .setConfirm(true)
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        Payment payment = Payment.builder()
                .user(user)
                .paymentIntentId(paymentIntent.getId())
                .amountInCents(paymentSessionRequest.price())
                .subscriptionType(SubscriptionType.valueOf(paymentSessionRequest.subscriptionType().toUpperCase()))
                .status(PaymentStatus.PENDING)
                .build();
        this.paymentRepository.saveAndFlush(payment);

        if (paymentIntent.getNextAction() != null && paymentIntent.getNextAction().getDisplayBankTransferInstructions() != null) {
            String iban = getIban(paymentIntent);
            File file = PDFWriterUtil.generateProFormaInvoicePDF(paymentSessionRequest, iban);
            String body = "Dear Customer,\n\nPlease find attached your pro forma invoice. "
                    + "Kindly complete the bank transfer to the provided IBAN to proceed with your order.\n\n"
                    + "Thank you,\nUnparalleled Team.";

            this.emailService.sendEmail(paymentSessionRequest.contactEmail(), "Payment details", body, file);
        }
        Map<String, String> metadata = new HashMap<>();
        metadata.put("customer_id", stripeCustomerId);
        metadata.put("email", paymentSessionRequest.contactEmail());
        metadata.put("url", "pending");
        return metadata;
    }

    private static String getIban(PaymentIntent paymentIntent) {
        PaymentIntent.NextAction.DisplayBankTransferInstructions instructions = paymentIntent.getNextAction().getDisplayBankTransferInstructions();

        String iban = "";

        if (instructions.getFinancialAddresses() != null && !instructions.getFinancialAddresses().isEmpty()) {
            for (PaymentIntent.NextAction.DisplayBankTransferInstructions.FinancialAddress address : instructions.getFinancialAddresses()) {
                if ("iban".equals(address.getType()) && address.getIban() != null) {
                    iban = address.getIban().getIban();
                }
            }
        }
        return iban;
    }

    private Map<String, String> createCardCheckoutSession(PaymentSessionRequest request, User user) throws StripeException {
        String subscriptionType = request.subscriptionType().toUpperCase();
        String email = request.contactEmail();

        SessionCreateParams params = SessionCreateParams
                .builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:4200/success?sessionId={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:4200/cancel")
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setCurrency("eur")
                .putMetadata("email", email)
                .putMetadata("subscriptionType", subscriptionType)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData
                                .builder()
                                .setCurrency("eur")
                                .setUnitAmount(request.price()).setProductData(SessionCreateParams.LineItem.PriceData.ProductData
                                        .builder()
                                        .setName("One-time payment")
                                        .build())
                                .build())
                        .build())
                .build();
        Session session = Session.create(params);
        String sessionId = session.getId();

        Payment payment = Payment.builder().user(user).sessionId(sessionId).amountInCents(request.price()).subscriptionType(SubscriptionType.valueOf(subscriptionType.toUpperCase())).status(PaymentStatus.PENDING).build();
        this.paymentRepository.saveAndFlush(payment);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("customer_id", user.getStripeCustomerId() != null ? user.getStripeCustomerId() : "");
        metadata.put("email", request.contactEmail());
        metadata.put("url", sessionId);
        return metadata;
    }

    public Map<String, String> handleSuccessfulPayment(String payload, String signatureHeader) {
        Map<String, String> body = new HashMap<>();
        Payment payment = null;
        try {
            Event event = Webhook.constructEvent(payload, signatureHeader, endpointCheckoutCompletedSecret);

            if (EVENT_TYPE_CHECKOUT_SESSION.equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    String email = session.getMetadata().get("email");
                    String subscriptionType = session.getMetadata().get("subscriptionType");

                    String result = this.userService.updateUserSubscription(subscriptionType, email);
                    body.put("message", result);
                    body.put("status", "success");
                    String sessionId = session.getId();
                    payment = this.paymentRepository.findPaymentBySessionId(sessionId).orElseThrow(() -> new PaymentNotFoundException(sessionId));
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

        return new PaymentStatusResponse(payment.getStatus().name().toLowerCase(), "Successful!");
    }

    public Map<String, String> handleSuccessfulInvoicePayment(String payload, String signatureHeader) {
        Map<String, String> body = new HashMap<>();
        Payment payment = null;

        try {
            Event event = Webhook.constructEvent(payload, signatureHeader, endpointCheckoutCompletedSecret);
            if (EVENT_TYPE_PAYMENT_INTENT.equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (paymentIntent != null) {
                    String invoiceId = paymentIntent.getInvoice();
                    Invoice retrieve = Invoice.retrieve(invoiceId);
                    String invoicePdf = retrieve.getInvoicePdf();
                    String paymentIntentId = paymentIntent.getId();

                    payment = this.paymentRepository
                            .findPaymentByInvoiceId(invoiceId)
                            .orElseThrow(() ->
                                    new PaymentNotFoundException(invoiceId));

                    payment.setStatus(PaymentStatus.SUCCEEDED);
                    payment.setUpdatedAt(LocalDateTime.now());
                    this.paymentRepository.saveAndFlush(payment);

                    String customerId = retrieve.getCustomer();

                    User user = this.userRepository.findByStripeCustomerId(customerId).orElseThrow(() -> new UserDoesNotExistException(customerId));
                    String subscriptionType = retrieve.getMetadata().get("subscriptionType");
                    String result = this.userService.updateUserSubscription(subscriptionType, user.getEmail());

                    body.put("message", result);
                    body.put("status", "success");
                }
            }

        } catch (SignatureVerificationException e) {
            if (payment != null) {
                payment.setStatus(PaymentStatus.FAILED);
                this.paymentRepository.saveAndFlush(payment);
            }
            throw new RuntimeException("Invalid signature: " + e.getMessage(), e);
        } catch (Exception e) {
            if (payment != null) {
                payment.setStatus(PaymentStatus.FAILED);
                this.paymentRepository.saveAndFlush(payment);
            }
            throw new RuntimeException("Error processing payment intent: " + e.getMessage(), e);
        }
        if (!body.containsKey("status")) {
            body.put("status", "failed");
        }
        return body;
    }

//    private String createInvoiceForSuccessfulPayment(PaymentIntent paymentIntent) throws StripeException {
//        InvoiceItemCreateParams invoiceItemParams =
//                InvoiceItemCreateParams.builder()
//                        .setCustomer(paymentIntent.getCustomer())
//                        .setCurrency(paymentIntent.getCurrency())
//                        .setAmount(paymentIntent.getAmount())
//                        .setDescription("Charge for: " + paymentIntent.getDescription())
//                        .build();
//
//        InvoiceItem invoiceItem = InvoiceItem.create(invoiceItemParams);
//
//        InvoiceCreateParams invoiceParams = InvoiceCreateParams.builder()
//                .setCustomer(paymentIntent.getCustomer())
//                .setCollectionMethod(InvoiceCreateParams.CollectionMethod.SEND_INVOICE)
//                .setDueDate(System.currentTimeMillis() + 1000L)
//                .setAutoAdvance(true)
//                .build();
//
//        Invoice invoice = Invoice.create(invoiceParams);
//
//        invoice.finalizeInvoice();
//        invoice.sendInvoice();
//
//        return invoice.getId();
//    }
}
