package com.waimai.platform.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTests {

    private final JwtService jwtService = new JwtService(
            "test-only-jwt-secret-with-at-least-thirty-two-bytes",
            120
    );

    @Test
    void generatedTokenCanBeParsedAndValidated() {
        String token = jwtService.generateToken("test_user");

        assertEquals("test_user", jwtService.extractUsername(token));
        assertTrue(jwtService.isValid(token, "test_user"));
        assertFalse(jwtService.isValid(token, "another_user"));
        assertEquals(7200, jwtService.getExpirationSeconds());
    }

    @Test
    void rejectsNonPositiveExpiration() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService(
                "test-only-jwt-secret-with-at-least-thirty-two-bytes",
                0
        ));
    }
}
