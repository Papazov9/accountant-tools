package com.wildrep.accountantapp.model;

import com.wildrep.accountantapp.model.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private Gender gender;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "subscription_start")
    private LocalDate subscriptionStart;

    @Column(name = "subscription_end")
    private LocalDate subscriptionEnd;

    @Column(name = "comparison_count")
    private Integer comparisonCount;

    @Column(name = "is_acknowledged")
    private boolean isAcknowledged;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Column(name = "stripe_customer_id", unique = true)
    private String stripeCustomerId;

    @Column(name = "code_expiration_date")
    private LocalDateTime codeExpirationDate;


    @Column(name = "is_verified")
    private boolean isVerified = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public void addComparisons(int amount) {
        if (subscription != null) {
            this.comparisonCount += amount;
        }
    }

    public void decrementComparisonCount() {
        if (canMakeComparison()) {
            this.comparisonCount--;
        }
    }

    public boolean canMakeComparison() {
        return comparisonCount != null && comparisonCount > 0;
    }

}