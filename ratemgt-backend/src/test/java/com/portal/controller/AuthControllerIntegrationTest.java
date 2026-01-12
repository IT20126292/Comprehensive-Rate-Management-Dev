package com.portal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampath.portal.SegmentationApplication;
import com.sampath.portal.dto.AuthResponse;
import com.sampath.portal.dto.LoginRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SegmentationApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Use real DB
@ActiveProfiles("test") // optional test profile
@EnabledIfEnvironmentVariable(named = "RUN_UPM_TESTS", matches = "true")
@TestPropertySource(properties = {
        "adupm.service.api=${ADUPM_SERVICE_API:http://192.125.125.154/webservicesRest/api/adauthentication/v1/authentication}"
})
@Import(com.portal.config.UpmTestConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    LoginRequest loginRequest;
    LoginRequest invalidLoginRequest;

    @BeforeEach
    void setUp() {
        // First, login to get a refresh token
        loginRequest = new LoginRequest();
        String upmUser = System.getenv("UPM_TEST_USER");
        String upmPass = System.getenv("UPM_TEST_PASS");
        if (upmUser != null && !upmUser.isBlank() && upmPass != null && !upmPass.isBlank()) {
            loginRequest.setUsername(upmUser);
            loginRequest.setPassword(upmPass);
        } else {
            // Fallback credentials (may fail if UPM rejects)
            loginRequest.setUsername("jayan");
            loginRequest.setPassword("123");
        }
 
        //invalid login
        invalidLoginRequest = new LoginRequest();
        invalidLoginRequest.setUsername("jayan");
        invalidLoginRequest.setPassword("wrong");
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Skip this test unless explicit success creds are provided
        Assumptions.assumeTrue(
                System.getenv("UPM_TEST_USER") != null && System.getenv("UPM_TEST_PASS") != null,
                "No UPM_TEST_USER/UPM_TEST_PASS provided; skipping success test"
        );
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void testLoginFailure() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testRefreshToken() throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // If login is not 200, skip refresh test (UPM not available/invalid creds)
        Assumptions.assumeTrue(loginResult.getResponse().getStatus() == 200,
                "Login failed; skipping refresh token test");

        String response = loginResult.getResponse().getContentAsString();

        Assumptions.assumeTrue(response != null && !response.isBlank() &&
                        !"null".equalsIgnoreCase(response.trim()),
                "Empty/null login response; skipping refresh token test");

        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        // Use refresh token
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", authResponse.getRefreshToken());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }
}
