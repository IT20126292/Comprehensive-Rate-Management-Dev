package com.sampath.portal.service;


import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sampath.portal.dto.AuthResponse;
import com.sampath.portal.dto.LoginRequest;
import com.sampath.portal.dto.MockAuthRequest;
import com.sampath.portal.dto.MockAuthResponse;
import com.sampath.portal.service.impl.UserDetailsServiceImpl;
import com.sampath.portal.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    @Value("${adupm.service.api}")
    private String adUpmServiceApi;

    @Value("${adupm.basic.auth}")
    private String adUpmBasicAuth;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder; // Injected
    private final RestTemplate restTemplate;

    public AuthResponse authenticate(LoginRequest request) {

        if(request.getUsername() != null){
            request.setUsername(request.getUsername().trim().toUpperCase());
        }
        log.info("Authenticate request received for user: {}", request.getUsername());
        
        try {
            
            String timeIst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
            MockAuthRequest mockRequest = new MockAuthRequest();
            mockRequest.setRequestTime(timeIst);
            mockRequest.setAdUsername(request.getUsername());
            mockRequest.setAdUserPassword(request.getPassword());
            mockRequest.setAppCode("USP");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", adUpmBasicAuth);
            headers.set("ServiceName","ADAuthentication");
            headers.set("TokenID","0");
            HttpEntity <MockAuthRequest> entity = new HttpEntity<>(mockRequest,headers);

            log.info("AD service : "+ adUpmServiceApi);
            log.info("full "+ entity );
            
            ResponseEntity <String> response = restTemplate.exchange(adUpmServiceApi, HttpMethod.POST,entity,String.class);
            log.info("api response :"+ response.getBody());
            
            MockAuthResponse mockResponse = restTemplate.postForObject(
                        adUpmServiceApi,
                        entity,
                        MockAuthResponse.class
            );
            log.info("full resp :"+ mockResponse);
            log.info("mockResponse ok : " + mockResponse.getMessage());

            if (mockResponse.getMessage() != null) {
                throw new RuntimeException("Invalid login");
            }

            log.info("mockResponse role : " + mockResponse.getUserClass());

            List<String> roles;
            if ("50".equals(mockResponse.getUserClass())) {
                roles = List.of("ADMIN");
            } else if ("10".equals(mockResponse.getUserClass())) {
                roles = List.of("USER");
            } else {
                throw new RuntimeException("Invalid login");
            }

            String accessToken = jwtUtil.generateAccessToken(request.getUsername(), roles);
            String refreshToken = jwtUtil.generateRefreshToken(request.getUsername(), roles);

            return new AuthResponse(accessToken, refreshToken, request.getUsername(), roles);

        } catch (BadCredentialsException ex) {
            throw new RuntimeException("Invalid username or password");
        } catch (Exception ex) {
            log.error("Authentication failed for user: {}", request.getUsername(), ex);
            throw new RuntimeException("Authentication service failed: " + ex.getMessage());
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Invalid refresh token provided");
            throw new RuntimeException("Invalid Refresh Token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails user = userDetailsService.loadUserByUsername(username);

        if (user == null) {
            log.error("User not found during refresh: {}", username);
            throw new RuntimeException("User not found");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user);
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("Refresh token generated for user: {}", username);
        return new AuthResponse(newAccessToken, refreshToken, username, roles);
    }
}
