package com.wildrep.accountantapp.config;

import com.wildrep.accountantapp.model.Role;
import com.wildrep.accountantapp.model.Subscription;
import com.wildrep.accountantapp.model.enums.RoleEnum;
import com.wildrep.accountantapp.model.enums.SubscriptionType;
import com.wildrep.accountantapp.repo.RoleRepository;
import com.wildrep.accountantapp.repo.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DBInit implements CommandLineRunner {

    private final SubscriptionRepository subscriptionRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (this.roleRepository.count() == 0) {
        initRoles();
        }

        if (this.subscriptionRepository.count() == 0) {
        initSubscriptions();
        }
    }

    private void initSubscriptions() {
        Arrays.stream(SubscriptionType.values()).forEach(s -> {
            Subscription subscription = new Subscription();

            switch (s) {
                case FREE -> {
                    subscription.setType(s);
                    subscription.setPrice(0.0);
                    subscription.setMaxComparisons(1);
                    subscription.setDescription("Basic subscription type with very limited rights.");
                }
                case PREMIUM -> {
                    subscription.setType(s);
                    subscription.setPrice(9.99);
                    subscription.setMaxComparisons(10);
                    subscription.setDescription("Premium tier subscription. Best for private use by 1 person.");
                }
                case BUSINESS -> {
                    subscription.setType(s);
                    subscription.setPrice(19.99);
                    subscription.setMaxComparisons(20);
                    subscription.setDescription("Business tier subscription. Suitable for small business.");
                }
                case ENTERPRISE -> {
                    subscription.setType(s);
                    subscription.setPrice(29.99);
                    subscription.setMaxComparisons(30);
                    subscription.setDescription("Enterprise tier subscription. The ULTIMATE choice for UNPARALLELED results!");
                }
            }
            this.subscriptionRepository.saveAndFlush(subscription);
        });
    }

    private void initRoles() {
        Arrays.stream(RoleEnum.values()).forEach(r -> {
            Role role = new Role();
            role.setRoleName(r);
            this.roleRepository.saveAndFlush(role);
        });
    }


}
