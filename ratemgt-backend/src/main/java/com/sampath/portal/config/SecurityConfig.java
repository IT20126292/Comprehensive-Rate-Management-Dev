package com.sampath.portal.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sampath.portal.service.impl.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.cors.allowedOriginWithPort:}")
    private String allowedOriginWithPort;

    @Value("${app.cors.allowedOrigin:}")
    private String allowedOrigin;

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder encoder)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                   .userDetailsService(userDetailsService)
                   .passwordEncoder(encoder)
                   .and()
                   .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        http
            .cors(cors -> {}) // enable CORS
            .csrf(csrf -> csrf
            .csrfTokenRequestHandler(requestHandler)
            .csrfTokenRepository(cookieRepo()))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public CookieCsrfTokenRepository cookieRepo() {
        CookieCsrfTokenRepository repo = new CookieCsrfTokenRepository();
        // Ensure HTTPS-friendly cookie for cross-site usage in dev/prod
        repo.setCookieCustomizer(builder -> builder
            .sameSite("None")
            .secure(true)
            .path("/")
        );
        return repo;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Resolve and apply allowed origins (empty list blocks cross-origin)
        java.util.List<String> origins = resolveAllowedOrigins();
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply CORS to all endpoints
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Build ordered, de-duplicated origins from configured CSV properties
    private java.util.List<String> resolveAllowedOrigins() {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        addCsvValues(allowedOriginWithPort, set);
        addCsvValues(allowedOrigin, set);
        return java.util.List.copyOf(set);
    }

    private static void addCsvValues(String csv, java.util.Set<String> out) {
        if (csv == null || csv.isBlank()) return;
        for (String token : csv.split(",")) {
            String v = token.trim();
            if (!v.isEmpty()) out.add(v);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}