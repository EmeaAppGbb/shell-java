package com.spec2cloud.api.controller;

import com.spec2cloud.api.model.ErrorResponse;
import com.spec2cloud.api.model.UserResponse;
import com.spec2cloud.api.service.UserStore;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserStore userStore;

    public AdminController(UserStore userStore) {
        this.userStore = userStore;
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Claims claims)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Not authenticated"));
        }

        String role = claims.get("role", String.class);
        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("Forbidden"));
        }

        var users = userStore.findAll().stream()
            .map(UserResponse::from)
            .toList();
        return ResponseEntity.ok(users);
    }
}
