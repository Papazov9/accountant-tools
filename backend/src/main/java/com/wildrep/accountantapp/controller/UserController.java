package com.wildrep.accountantapp.controller;

import com.wildrep.accountantapp.exceptions.UserDoesNotExistException;
import com.wildrep.accountantapp.model.Subscription;
import com.wildrep.accountantapp.model.dto.DashboardUserResponse;
import com.wildrep.accountantapp.model.enums.SubscriptionType;
import com.wildrep.accountantapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/dashboard/{username}")
    public ResponseEntity<DashboardUserResponse> loadDashboardData(@PathVariable String username) {
        DashboardUserResponse dashboardUserResponse = this.userService.loadUserData(username);
        return ResponseEntity.ok(dashboardUserResponse);
    }

    @GetMapping("/free/{username}")
    public ResponseEntity<String> freeSubscription(@PathVariable String username) {
        try {
            String result = this.userService.updateUserSubscription(SubscriptionType.FREE, username);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
