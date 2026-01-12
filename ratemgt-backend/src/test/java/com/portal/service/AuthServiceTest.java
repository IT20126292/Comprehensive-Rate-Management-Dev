package com.portal.service;

import com.sampath.portal.dto.AuthResponse;
import com.sampath.portal.dto.LoginRequest;
import com.sampath.portal.dto.MockAuthResponse;

import com.sampath.portal.service.AuthService;

import com.sampath.portal.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock JwtUtil jwtUtil;

    @Mock RestTemplate restTemplate;

    @InjectMocks AuthService authService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(authService, "adUpmServiceApi", "http://upm.example/api");
    }

    @Test
    void authenticate_admin_role_ok() {
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("pw");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{}"));

        MockAuthResponse mock = new MockAuthResponse();
        mock.setMessage(null);
        mock.setUserClass("50"); // ADMIN branch
        when(restTemplate.postForObject(anyString(), any(), eq(MockAuthResponse.class))).thenReturn(mock);

        when(jwtUtil.generateAccessToken(eq("alice"), anyList())).thenReturn("at");
        when(jwtUtil.generateRefreshToken(eq("alice"), anyList())).thenReturn("rt");

        AuthResponse resp = authService.authenticate(req);
        assertEquals("alice", resp.getUsername());
        assertEquals("at", resp.getAccessToken());
        assertEquals("rt", resp.getRefreshToken());
        assertEquals(List.of("ADMIN"), resp.getRoles());
    }

    @Test
    void authenticate_user_role_ok() {
        LoginRequest req = new LoginRequest();
        req.setUsername("bob");
        req.setPassword("pw");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{}"));
        MockAuthResponse mock = new MockAuthResponse();
        mock.setMessage(null);
        mock.setUserClass("10"); // USER branch
        when(restTemplate.postForObject(anyString(), any(), eq(MockAuthResponse.class))).thenReturn(mock);
        when(jwtUtil.generateAccessToken(eq("bob"), anyList())).thenReturn("at2");
        when(jwtUtil.generateRefreshToken(eq("bob"), anyList())).thenReturn("rt2");

        AuthResponse resp = authService.authenticate(req);
        assertEquals(List.of("USER"), resp.getRoles());
        assertEquals("at2", resp.getAccessToken());
        assertEquals("rt2", resp.getRefreshToken());
    }

    @Test
    void authenticate_failure_message_present() {
        LoginRequest req = new LoginRequest();
        req.setUsername("bad");
        req.setPassword("pw");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{}"));
        MockAuthResponse mock = new MockAuthResponse();
        mock.setMessage("Invalid");
        when(restTemplate.postForObject(anyString(), any(), eq(MockAuthResponse.class))).thenReturn(mock);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.authenticate(req));
        assertTrue(ex.getMessage().contains("Invalid login") || ex.getMessage().contains("Authentication service failed"));
    }



    @Test
    void refresh_token_invalid() {
        when(jwtUtil.validateToken("bad")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> authService.refreshToken("bad"));
    }
}

