package edu.uvg.gestionpedidos.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncoderTest {

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    @Test
    void passwordCorrecta() {

        String password = "PruebaBCrypt2026!";
        String hash = encoder.encode(password);

        assertTrue(encoder.matches(password, hash));
    }

    @Test
    void passwordIncorrecta() {

        String password = "PruebaBCrypt2026!";
        String hash = encoder.encode(password);

        assertFalse(encoder.matches("OtraPassword123!", hash));
    }

    @Test
    void hashesDiferentes() {

        String password = "PruebaBCrypt2026!";

        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);

        assertNotEquals(hash1, hash2);

        assertTrue(encoder.matches(password, hash1));
        assertTrue(encoder.matches(password, hash2));
    }
}