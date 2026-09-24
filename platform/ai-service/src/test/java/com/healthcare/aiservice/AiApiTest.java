package com.healthcare.aiservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AiApiTest {
    @Autowired MockMvc mvc;
    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void guardrailsHold() throws Exception {
        String patient = token("p", "PATIENT");
        mvc.perform(post("/api/v1/ai/triage").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON).content("{\"symptoms\":\"I have chest pain and difficulty breathing\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.urgency").value("EMERGENCY"));
        mvc.perform(post("/api/v1/ai/triage").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON).content("{\"symptoms\":\"who will win the football match\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.refused").value(true));
        mvc.perform(post("/api/v1/ai/triage")).andExpect(status().isUnauthorized());
    }
}
