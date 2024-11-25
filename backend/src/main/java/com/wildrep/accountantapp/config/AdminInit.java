package com.wildrep.accountantapp.config;

import com.wildrep.accountantapp.exceptions.SubscriptionNotFoundException;
import com.wildrep.accountantapp.model.Role;
import com.wildrep.accountantapp.model.Subscription;
import com.wildrep.accountantapp.model.User;
import com.wildrep.accountantapp.model.enums.Gender;
import com.wildrep.accountantapp.model.enums.RoleEnum;
import com.wildrep.accountantapp.model.enums.SubscriptionType;
import com.wildrep.accountantapp.repo.RoleRepository;
import com.wildrep.accountantapp.repo.SubscriptionRepository;
import com.wildrep.accountantapp.repo.UserRepository;
import com.wildrep.accountantapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminInit {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void adminInit() throws RoleNotFoundException {
        if (userRepository.findByRole(RoleEnum.ADMIN).isEmpty()) {
            Subscription unparalleledSub = subscriptionRepository.findByType(SubscriptionType.UNPARALLELED).orElseThrow(() -> new SubscriptionNotFoundException("Subscription UNPARALLELED not found!"));
            Role adminRole = this.roleRepository.findByRoleName(RoleEnum.ADMIN).orElseThrow(() -> new RoleNotFoundException("Role ADMIN not found!"));
            Role userRole = this.roleRepository.findByRoleName(RoleEnum.USER).orElseThrow(() -> new RoleNotFoundException("Role USER not found!"));
            User admin = User.builder()
                    .isVerified(true)
                    .username("DimAdminBlago")
                    .email("blagovestpapazov987@gmail.com")
                    .password(passwordEncoder.encode("UnparalleledBeautiful#1912"))
                    .firstName("DimAdminBlago")
                    .lastName("Admin")
                    .comparisonCount(unparalleledSub.getMaxComparisons())
                    .subscription(unparalleledSub)
                    .gender(Gender.MALE)
                    .roles(Set.of(adminRole, userRole))
                    .build();

            this.emailService.sendEmail(admin.getEmail(), "Admin created", "Admin with username: " + admin.getUsername() + " and password: UnparalleledBeautiful#1912 was created!", null);
            this.userRepository.saveAndFlush(admin);
        }
    }
}
