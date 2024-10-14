package com.wildrep.accountantapp.controller;

import com.wildrep.accountantapp.model.dto.LoginRequest;
import com.wildrep.accountantapp.model.dto.LoginResponse;
import com.wildrep.accountantapp.model.dto.RegisterRequest;
import com.wildrep.accountantapp.model.dto.RegisterResponse;
import com.wildrep.accountantapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            RegisterResponse registerResponse = this.userService.registerUser(registerRequest);
            return ResponseEntity.ok(registerResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new RegisterResponse(e.getMessage(), registerRequest.username()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            LoginResponse loginResponse = this.userService.loginUser(loginRequest);
            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Invalid username or password!", null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse("Unexpected error occurred!", null, null));
        }

    }
}
