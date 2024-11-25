package com.wildrep.accountantapp.model;

import com.wildrep.accountantapp.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String companyName;

    private String bulstat;
    private Boolean vatRegistered;

    @Column(nullable = false, name = "google_drive_link")
    private String googleDriveLink;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String country;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    private String subscriptionType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String googleDriveFileId;

    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL)
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
