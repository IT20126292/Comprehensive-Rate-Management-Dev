package com.portal.util;

import com.sampath.portal.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void init() {
        jwtUtil = new JwtUtil();
        // Set a 512-bit (64-byte) base64-encoded secret: 88 'A' chars decode to 64 zero bytes
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    @Test
    void access_token_roundtrip() {
        String token = jwtUtil.generateAccessToken("sandunip", List.of("USER"));
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("sandunip", jwtUtil.extractUsername(token));
        assertEquals(List.of("USER"), jwtUtil.extractRoles(token));
    }

    @Test
    void refresh_token_roundtrip() {
        String token = jwtUtil.generateRefreshToken("kaushanas", List.of("ADMIN"));
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("kaushanas", jwtUtil.extractUsername(token));
        assertEquals(List.of("ADMIN"), jwtUtil.extractRoles(token));
    }
}
