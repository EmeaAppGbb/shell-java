package com.spec2cloud.api;

import com.spec2cloud.api.service.UserStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserStore userStore;

    @BeforeEach
    void resetStore() {
        userStore.clear();
    }

    // ---- Register ----

    @Test
    void register_happyPath_returns201AndCookie() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "alice", "password": "password123"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Registration successful"))
            .andExpect(cookie().exists("token"))
            .andExpect(cookie().httpOnly("token", true));
    }

    @Test
    void register_firstUserIsAdmin() throws Exception {
        // First user
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "admin_user", "password": "password123"}
                    """))
            .andExpect(status().isCreated());

        // Login and check role via /me
        var loginResult = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "admin_user", "password": "password123"}
                    """))
            .andExpect(status().isOk())
            .andReturn();

        Cookie tokenCookie = loginResult.getResponse().getCookie("token");

        mvc.perform(get("/api/auth/me").cookie(tokenCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("admin"));
    }

    @Test
    void register_secondUserIsRegularUser() throws Exception {
        // First user (admin)
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "first", "password": "password123"}
                    """))
            .andExpect(status().isCreated());

        // Second user (regular)
        var regResult = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "second", "password": "password123"}
                    """))
            .andExpect(status().isCreated())
            .andReturn();

        Cookie tokenCookie = regResult.getResponse().getCookie("token");

        mvc.perform(get("/api/auth/me").cookie(tokenCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("user"));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "alice", "password": "password123"}
                    """))
            .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "alice", "password": "otherpassword"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void register_invalidUsername_returns400() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "ab", "password": "password123"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Username must be")));
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "validuser", "password": "short"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Password must be")));
    }

    @Test
    void register_missingFields_returns400() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "", "password": ""}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Username and password are required"));
    }

    // ---- Login ----

    @Test
    void login_happyPath_returns200AndCookie() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "alice", "password": "password123"}
                    """));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "alice", "password": "password123"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(cookie().exists("token"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "alice", "password": "password123"}
                    """));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "alice", "password": "wrong"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void login_nonexistentUser_returns401() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "nobody", "password": "password123"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    // ---- Logout ----

    @Test
    void logout_clearsCookie() throws Exception {
        mvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out successfully"))
            .andExpect(cookie().maxAge("token", 0));
    }

    // ---- Me ----

    @Test
    void me_authenticated_returnsUserInfo() throws Exception {
        var regResult = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "alice", "password": "password123"}
                    """))
            .andReturn();

        Cookie tokenCookie = regResult.getResponse().getCookie("token");

        mvc.perform(get("/api/auth/me").cookie(tokenCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.role").exists())
            .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void me_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Not authenticated"));
    }
}
