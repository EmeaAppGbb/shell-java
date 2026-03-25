package com.spec2cloud.api.controller;

import com.spec2cloud.api.model.*;
import com.spec2cloud.api.service.AuthService;
import com.spec2cloud.api.service.AuthService.LoginResult;
import com.spec2cloud.api.service.AuthService.RegisterResult;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final int COOKIE_MAX_AGE = 86400; // 24 hours in seconds

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req, HttpServletResponse response) {
        var result = authService.register(req.username(), req.password());

        if (result instanceof RegisterResult.Success s) {
            addTokenCookie(response, authService.generateToken(s.user()));
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new MessageResponse("Registration successful"));
        } else if (result instanceof RegisterResult.ValidationError e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.message()));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Username already exists"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        var result = authService.login(req.username(), req.password());

        if (result instanceof LoginResult.Success s) {
            addTokenCookie(response, authService.generateToken(s.user()));
            return ResponseEntity.ok(new MessageResponse("Login successful"));
        } else if (result instanceof LoginResult.BadCredentials e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.message()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid credentials"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        var cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Claims claims)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Not authenticated"));
        }
        return authService.getCurrentUser((String) authentication.getCredentials())
            .<ResponseEntity<?>>map(user -> ResponseEntity.ok(UserResponse.from(user)))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Not authenticated")));
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        var cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }
}
