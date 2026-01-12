package com.sampath.portal.util;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Component
public class JwtUtil {

    private final String roleLiteral = "roles";


    @Value("${jwt.secret}")
    private String secretKey;

    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 15; // 15 minutes
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24; // 24 hours

    // --- Existing method (UserDetails) ---
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
    }

    // --- New method (username + roles) ---
    public String generateAccessToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim(roleLiteral, roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
    }

    public String generateRefreshToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim(roleLiteral, roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // --- Existing helper methods ---
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .get(roleLiteral);
    }


}

