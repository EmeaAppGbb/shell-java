package com.spec2cloud.api.middleware;

import com.spec2cloud.api.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public JwtAuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        extractTokenFromCookies(request).ifPresent(token ->
            authService.validateToken(token).ifPresent(claims -> {
                var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class).toUpperCase())
                );
                var authentication = new UsernamePasswordAuthenticationToken(
                    claims, token, authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            })
        );

        filterChain.doFilter(request, response);
    }

    private java.util.Optional<String> extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return java.util.Optional.empty();
        return Arrays.stream(request.getCookies())
            .filter(c -> "token".equals(c.getName()))
            .map(Cookie::getValue)
            .filter(v -> !v.isBlank())
            .findFirst();
    }
}
