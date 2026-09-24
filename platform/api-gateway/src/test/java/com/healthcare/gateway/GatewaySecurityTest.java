package com.healthcare.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GatewaySecurityTest {
    @Autowired MockMvc mvc;
    @Value("${jwt.secret}") String secret;

    String token(boolean expired) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        long exp = expired ? System.currentTimeMillis() - 1000 : System.currentTimeMillis() + 600000;
        return Jwts.builder().setSubject("u").claim("roles", List.of("PATIENT"))
                .setIssuedAt(new Date()).setExpiration(new Date(exp))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void protectedPathsRequireValidToken() throws Exception {
        mvc.perform(get("/api/v1/patients/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get("/api/v1/patients/1").header("Authorization", "Bearer bogus"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
        mvc.perform(get("/api/v1/patients/1").header("Authorization", "Bearer " + token(true)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void correlationIdIsPropagated() throws Exception {
        mvc.perform(get("/gateway/health").header("X-Correlation-Id", "corr-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "corr-123"));
        mvc.perform(get("/gateway/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }
}
