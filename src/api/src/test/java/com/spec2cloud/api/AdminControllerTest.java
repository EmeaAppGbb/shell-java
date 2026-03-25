package com.spec2cloud.api;

import com.spec2cloud.api.service.UserStore;
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
class AdminControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserStore userStore;

    @BeforeEach
    void resetStore() {
        userStore.clear();
    }

    private Cookie registerAndGetToken(String username) throws Exception {
        var result = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "%s", "password": "password123"}
                    """.formatted(username)))
            .andReturn();
        return result.getResponse().getCookie("token");
    }

    @Test
    void listUsers_asAdmin_returnsAllUsers() throws Exception {
        Cookie adminToken = registerAndGetToken("admin_user");  // first = admin
        registerAndGetToken("regular_user");                     // second = user

        mvc.perform(get("/api/admin/users").cookie(adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].username", containsInAnyOrder("admin_user", "regular_user")));
    }

    @Test
    void listUsers_asRegularUser_returns403() throws Exception {
        registerAndGetToken("admin_user");                       // first = admin
        Cookie userToken = registerAndGetToken("regular_user");  // second = user

        mvc.perform(get("/api/admin/users").cookie(userToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void listUsers_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Not authenticated"));
    }
}
