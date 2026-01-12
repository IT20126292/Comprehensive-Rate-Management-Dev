package com.portal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampath.portal.SegmentationApplication;
import com.sampath.portal.controller.AuthController;
import com.sampath.portal.dto.AuthResponse;
import com.sampath.portal.dto.LoginRequest;
import com.sampath.portal.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = SegmentationApplication.class)
class AuthControllerWebMvcTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;

    @Test
    void login_ok() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("sandunip");
        req.setPassword("abc@12345678");

        when(authService.authenticate(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("at", "rt", "sandunip", List.of("USER")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("at"))
                .andExpect(jsonPath("$.refreshToken").value("rt"))
                .andExpect(jsonPath("$.username").value("sandunip"));
    }

    @Test
    void refresh_ok() throws Exception {
        var resp = new com.sampath.portal.dto.AuthResponse("at2", "rt2", "sandunip", java.util.List.of("USER"));
        when(authService.refreshToken("rt2")).thenReturn(resp);

        var body = new java.util.HashMap<String, String>();
        body.put("refreshToken", "rt2");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("at2"))
                .andExpect(jsonPath("$.refreshToken").value("rt2"))
                .andExpect(jsonPath("$.username").value("sandunip"));
    }
}
