package com.wildrep.accountantapp.service;

import com.stripe.exception.StripeException;
import com.stripe.model.InvoiceItem;
import com.stripe.model.Product;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.InvoiceItemCreateParams;
import com.stripe.param.InvoiceSendInvoiceParams;
import com.wildrep.accountantapp.controller.PaymentStatusResponse;
import com.wildrep.accountantapp.controller.PendingPayment;
import com.wildrep.accountantapp.exceptions.PaymentNotFoundException;
import com.wildrep.accountantapp.exceptions.SubscriptionNotFoundException;
import com.wildrep.accountantapp.exceptions.UserDoesNotExistException;
import com.wildrep.accountantapp.model.Invoice;
import com.wildrep.accountantapp.model.Payment;
import com.wildrep.accountantapp.model.Subscription;
import com.wildrep.accountantapp.model.User;
import com.wildrep.accountantapp.model.dto.PaymentSessionRequest;
import com.wildrep.accountantapp.model.enums.PaymentStatus;
import com.wildrep.accountantapp.model.enums.SubscriptionType;
import com.wildrep.accountantapp.repo.InvoiceRepository;
import com.wildrep.accountantapp.repo.PaymentRepository;
import com.wildrep.accountantapp.repo.SubscriptionRepository;
import com.wildrep.accountantapp.repo.UserRepository;
import com.wildrep.accountantapp.util.PDFWriterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.RequestContextFilter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RequestContextFilter requestContextFilter;

    @Value("${google.folder.id}")
    private String parentFolderId;

    private static final String PAYMENT_METHOD_CARD = "card";
    private static final String PAYMENT_METHOD_BANK_TRANSFER = "bank";

    private static final String EVENT_TYPE_CHECKOUT_SESSION = "checkout.session.completed";
    private static final String EVENT_TYPE_PAYMENT_INTENT = "payment_intent.succeeded";

    private final UserService userService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;
    private final SubscriptionRepository subscriptionRepository;
    private final GoogleDriveService googleDriveService;
    private final InvoiceRepository invoiceRepository;

    public Map<String, String> handlePaymentRequest(PaymentSessionRequest paymentSessionRequest) throws IOException {
        User user = this.userRepository
                .findByEmail(paymentSessionRequest
                        .contactEmail()).orElseThrow(() -> new UserDoesNotExistException(paymentSessionRequest.contactEmail()));

        return createProFormaPaymentIntent(paymentSessionRequest, user);
    }

    private Map<String, String> createProFormaPaymentIntent(PaymentSessionRequest paymentSessionRequest, User user) throws IOException {

        String invoiceNumber = generateNextInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .customerEmail(user.getEmail())
                .companyName(paymentSessionRequest.companyName())
                .bulstat(paymentSessionRequest.bulstat())
                .vatRegistered(paymentSessionRequest.isVatRegistration())
                .addressLine1(paymentSessionRequest.addressLine1())
                .addressLine2(paymentSessionRequest.addressLine2())
                .city(paymentSessionRequest.city())
                .postalCode(paymentSessionRequest.postalCode())
                .country(paymentSessionRequest.country())
                .totalAmount(BigDecimal.valueOf(paymentSessionRequest.price() / 100.0))
                .subscriptionType(paymentSessionRequest.subscriptionType())
                .build();

        Payment payment = Payment.builder()
                .user(user)
                .amountInCents(paymentSessionRequest.price())
                .subscriptionType(SubscriptionType.valueOf(paymentSessionRequest.subscriptionType().toUpperCase()))
                .status(PaymentStatus.PENDING)
                .invoice(invoice)
                .build();

        File file = PDFWriterUtil.generateProFormaInvoicePDF(paymentSessionRequest, invoiceNumber);
        String body = "Уважаеми клиент,\n\nПрикачено ще откриете Вашата проформа фактура." +
                "\nМоля, извършете банковия превод към предоставената банкова сметка (IBAN), за да продължите с поръчката си." +
                "\nВъзможно най-скоро след успешен превод по сметката с правилно основание, Вашият акаунт ще бъде обновен." +
                "\nПри грешка, неполучена или неправилна поръчка, моля, свържете се с нас на посочения номер: 0899282694." +
                "\nБлагодарим Ви!\n\nЕкипът на Unparalleled";

        String webLink = this.googleDriveService.uploadFile(file.getAbsolutePath(), file.getName(), parentFolderId);
        invoice.setGoogleDriveLink(webLink);
        this.invoiceRepository.saveAndFlush(invoice);
        this.paymentRepository.saveAndFlush(payment);

        this.emailService.sendEmail(paymentSessionRequest.contactEmail(), "Детайли за плащане", body, file);


        Map<String, String> metadata = new HashMap<>();
        metadata.put("email", paymentSessionRequest.contactEmail());
        metadata.put("url", "pending");
        metadata.put("status", "success");
        metadata.put("webLink", webLink);
        return metadata;
    }

    private String generateNextInvoiceNumber() {
        Invoice firstByOrderByIdDesc = this.invoiceRepository.findFirstByOrderByIdDesc();
        long nextNumber = firstByOrderByIdDesc != null ? firstByOrderByIdDesc.getId() + 1 : 1;
        return String.format("UNP01-%010d", nextNumber);
    }

    public List<PendingPayment> loadPendingPayments() {
        return this.paymentRepository
                .findAllByStatus(PaymentStatus.PENDING)
                .stream()
                .map(p ->
                        new PendingPayment(
                                p.getInvoice().getInvoiceNumber(),
                                p.getStatus().name(),
                                p.getInvoice().getGoogleDriveLink(),
                                p.getUser().getEmail()))
                .toList();
    }

//    public PaymentStatusResponse getPaymentStatus(String sessionId) {
//       Payment payment = this.paymentRepository.findPaymentBySessionId(sessionId).orElseThrow(() -> new PaymentNotFoundException(sessionId));
//
//        return new PaymentStatusResponse(payment.getStatus().name().toLowerCase(), "Successful!");
//    }

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
