package com.wildrep.accountantapp.service;

import com.wildrep.accountantapp.exceptions.PasswordsDontMatchException;
import com.wildrep.accountantapp.exceptions.RoleDoesNotExist;
import com.wildrep.accountantapp.exceptions.UserAlreadyExistsException;
import com.wildrep.accountantapp.exceptions.UserDoesNotExistException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public RegisterResponse registerUser(@Valid RegisterRequest registerRequest) {
        if (this.userRepository.existsByUsername(registerRequest.username())) {
            throw new UserAlreadyExistsException(registerRequest.username());
        }

        Role role = this.roleRepository
                .findByRoleName(RoleEnum.USER)
                .orElseThrow(() -> new RoleDoesNotExist(RoleEnum.USER.name()));
        Subscription subscription = this.subscriptionRepository
                .findByType(SubscriptionType.FREE)
                .orElseThrow(() -> new IllegalArgumentException("Invalid subscription name!"));

        User user = User.builder()
                .username(registerRequest.username())
                .comparisonCount(1)
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .gender(Gender.valueOf(registerRequest.gender().toUpperCase()))
                .roles(Set.of(role))
                .subscription(subscription)
                .build();

        this.userRepository.saveAndFlush(user);

        return new RegisterResponse(String.format("User with username: %s registered successfully!", user.getUsername()), user.getUsername());
    }

    public LoginResponse loginUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(loginRequest.username());

        String jwtToken = this.jwtUtil.generateToken(authentication);

        return LoginResponse.builder()
                .message("Login successful!")
                .username(userDetails.getUsername())
                .token(jwtToken)
                .build();
    }

    public DashboardUserResponse loadUserData(String username) {

        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UserDoesNotExistException(username));

        return new DashboardUserResponse(user.getEmail(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getGender().name(), "User loaded successfully!");
    }

    public String createNewToken(String username) {
        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UserDoesNotExistException(username));

     return this.jwtUtil.generateNewToken(user);
    }
}
