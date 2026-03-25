package com.spec2cloud.api.service;

import com.spec2cloud.api.config.JwtConfig;
import com.spec2cloud.api.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserStore userStore;
    private final JwtConfig jwtConfig;
    private final SecretKey signingKey;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    public AuthService(UserStore userStore, JwtConfig jwtConfig) {
        this.userStore = userStore;
        this.jwtConfig = jwtConfig;
        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.secret().getBytes(StandardCharsets.UTF_8));
    }

    // ---- Registration ----

    public sealed interface RegisterResult {
        record Success(User user) implements RegisterResult {}
        record ValidationError(String message) implements RegisterResult {}
        record Duplicate() implements RegisterResult {}
    }

    public RegisterResult register(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return new RegisterResult.ValidationError("Username and password are required");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return new RegisterResult.ValidationError(
                "Username must be between 3 and 30 characters and contain only letters, numbers, and underscores");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new RegisterResult.ValidationError("Password must be at least 8 characters");
        }
        if (userStore.findByUsername(username).isPresent()) {
            return new RegisterResult.Duplicate();
        }

        String role = userStore.count() == 0 ? "admin" : "user";
        var user = new User(
            UUID.randomUUID().toString(),
            username,
            passwordEncoder.encode(password),
            role,
            Instant.now()
        );
        userStore.save(user);
        return new RegisterResult.Success(user);
    }

    // ---- Login ----

    public sealed interface LoginResult {
        record Success(User user) implements LoginResult {}
        record BadCredentials(String message) implements LoginResult {}
    }

    public LoginResult login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return new LoginResult.BadCredentials("Username and password are required");
        }
        Optional<User> maybeUser = userStore.findByUsername(username);
        if (maybeUser.isEmpty() || !passwordEncoder.matches(password, maybeUser.get().passwordHash())) {
            return new LoginResult.BadCredentials("Invalid username or password");
        }
        return new LoginResult.Success(maybeUser.get());
    }

    // ---- JWT ----

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.id())
            .claim("username", user.username())
            .claim("role", user.role())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(jwtConfig.expiration())))
            .signWith(signingKey)
            .compact();
    }

    public Optional<Claims> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Optional<User> getCurrentUser(String token) {
        return validateToken(token)
            .flatMap(claims -> userStore.findById(claims.getSubject()));
    }
}
