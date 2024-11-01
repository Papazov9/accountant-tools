package com.wildrep.accountantapp.service;

import com.wildrep.accountantapp.exceptions.*;
import com.wildrep.accountantapp.model.Role;
import com.wildrep.accountantapp.model.Subscription;
import com.wildrep.accountantapp.model.User;
import com.wildrep.accountantapp.model.dto.*;
import com.wildrep.accountantapp.model.enums.Gender;
import com.wildrep.accountantapp.model.enums.RoleEnum;
import com.wildrep.accountantapp.model.enums.SubscriptionType;
import com.wildrep.accountantapp.repo.RoleRepository;
import com.wildrep.accountantapp.repo.SubscriptionRepository;
import com.wildrep.accountantapp.repo.UserRepository;
import com.wildrep.accountantapp.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuthenticationManager authenticationManager;
    private final UnparalleledUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public RegisterResponse registerUser(@Valid RegisterRequest registerRequest) {
        if (this.userRepository.existsByUsername(registerRequest.username())) {
            throw new UserAlreadyExistsException(registerRequest.username());
        }

        Role role = this.roleRepository.findByRoleName(RoleEnum.USER).orElseThrow(() -> new RoleDoesNotExist(RoleEnum.USER.name()));

        User user = User.builder().username(registerRequest.username()).comparisonCount(1).email(registerRequest.email()).password(passwordEncoder.encode(registerRequest.password())).firstName(registerRequest.firstName()).lastName(registerRequest.lastName()).gender(Gender.valueOf(registerRequest.gender().toUpperCase())).roles(Set.of(role)).isAcknowledged(false).confirmationCode(RandomStringUtils.randomAlphanumeric(6).toUpperCase()).codeExpirationDate(LocalDateTime.now().plusHours(1)).isVerified(false).build();

        this.userRepository.saveAndFlush(user);

        String emailContent = "<p>Hello,</p>" + "<p>Thank you for registering. Please confirm your email by entering this code:</p>" + "<h3>" + user.getConfirmationCode() + "</h3>" + "<p>The code will expire in 1 hour. If you didn't register, please ignore this email.</p>";

        this.emailService.sendEmail(user.getEmail(), "Account Verification", emailContent);

        return new RegisterResponse(String.format("User with username: %s registered successfully!", user.getUsername()), user.getUsername());
    }

    public LoginResponse loginUser(LoginRequest loginRequest) {

        User user = this.userRepository
                .findByUsername(loginRequest.username())
                .orElseThrow(() ->
                        new UsernameNotFoundException(loginRequest.username()));

        if (!user.isVerified()) {
            throw new UserNotVerifiedException(user.getUsername());
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(loginRequest.username());

        String jwtToken = this.jwtUtil.generateToken(authentication);

        return LoginResponse.builder().message("Login successful!").username(userDetails.getUsername()).token(jwtToken).build();
    }

    public DashboardUserResponse loadUserData(String username) {

        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UserDoesNotExistException(username));
        SubscriptionResponse subscriptionResponse;
        if (user.isAcknowledged()) {
            subscriptionResponse = new SubscriptionResponse(user.getSubscription().getType().name(), user.getSubscription().getPrice(), user.getComparisonCount(), user.getSubscription().getDescription());
        } else {
            subscriptionResponse = new SubscriptionResponse(null, null, null, null);
        }

        return new DashboardUserResponse(user.getEmail(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getGender().name(), user.isAcknowledged(), "User loaded successfully!", subscriptionResponse);
    }

    public String createNewToken(String username) {
        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UserDoesNotExistException(username));

        return this.jwtUtil.generateNewToken(user);
    }

    public String updateUserSubscription(String subscriptionType, String username) {
        SubscriptionType subscription;
        try {
            subscription = SubscriptionType.valueOf(subscriptionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid subscription type: " + subscriptionType);
        }
        Subscription sub = this.subscriptionRepository.findByType(subscription).orElseThrow(() -> new SubscriptionNotFoundException(String.format("Subscription with name: %s was not found", subscriptionType)));
        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UserDoesNotExistException(username));
        user.setSubscription(sub);
        user.setAcknowledged(true);
        user.addComparisons(sub.getMaxComparisons());

        this.userRepository.saveAndFlush(user);
        return String.format("Subscription with name: %s added successfully to user with username: %s!", subscription.name(), username);
    }

    public boolean confirmCode(CodeConfirmRequest codeConfirmRequest) {
        User user = this.userRepository.findByEmail(codeConfirmRequest.email()).orElseThrow(() -> new UserDoesNotExistException(codeConfirmRequest.email()));

        if (user.getConfirmationCode().equals(codeConfirmRequest.code()) && user.getCodeExpirationDate().isAfter(LocalDateTime.now())) {
            user.setVerified(true);
            user.setConfirmationCode(null);
            user.setCodeExpirationDate(null);
            this.userRepository.saveAndFlush(user);


            String emailContent = "<p>Hello,</p>" + "<p>Your email has been successfully verified. You can now log in to your account.</p>";
            this.emailService.sendEmail(user.getEmail(), "Email Verified", emailContent);
            return true;
        }

        return false;
    }
}
