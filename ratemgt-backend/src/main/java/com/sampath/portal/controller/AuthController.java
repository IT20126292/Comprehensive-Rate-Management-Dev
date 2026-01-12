package com.sampath.portal.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sampath.portal.dto.AuthResponse;
import com.sampath.portal.dto.LoginRequest;
import com.sampath.portal.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login request received for user: {}", request.getUsername());
        AuthResponse response = authService.authenticate(request);
        
        log.info("Login successful for user: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        log.debug("Refresh token request received");
        AuthResponse response = authService.refreshToken(refreshToken);
        log.debug("Refresh token returned successfully");
        return ResponseEntity.ok(response);
    }
}
