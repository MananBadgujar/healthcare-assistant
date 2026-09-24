package com.healthcare.assistant.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Date;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpiredJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void expiredJwt_returns401() throws Exception {
        // Create a JWT that expired 1 hour ago using a secure key for HS512
        String expiredToken = Jwts.builder()
                .setSubject("test")
                .setExpiration(new Date(System.currentTimeMillis() - 3600_000))
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS512))
                .compact();

        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", "Bearer " + expiredToken))
               .andExpect(status().isUnauthorized());
    }
}