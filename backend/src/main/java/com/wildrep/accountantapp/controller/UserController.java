package com.wildrep.accountantapp.controller;

import com.wildrep.accountantapp.exceptions.UserDoesNotExistException;
import com.wildrep.accountantapp.model.dto.DashboardUserResponse;
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
        try {
            DashboardUserResponse dashboardUserResponse = this.userService.loadUserData(username);

            return ResponseEntity.ok(dashboardUserResponse);
        } catch (UserDoesNotExistException e) {
            return ResponseEntity.badRequest().body(new DashboardUserResponse(null, null, null, null, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new DashboardUserResponse(null, null, null, null, null, "An error occurred while loading user info!"));
        }
    }

}
