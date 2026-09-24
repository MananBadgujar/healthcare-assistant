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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenAccessingProtectedEndpointWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/appointments"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void whenMalformedJwt_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", "Bearer not.a.valid.jwt"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void whenJwtSignedWithDifferentKey_thenUnauthorized() throws Exception {
        String tokenWithWrongKey = Jwts.builder()
                .setSubject("attacker")
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256))
                .compact();
        mockMvc.perform(get("/api/v1/appointments")
                        .header("Authorization", "Bearer " + tokenWithWrongKey))
               .andExpect(status().isUnauthorized());
    }
}