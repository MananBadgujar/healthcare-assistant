package com.healthcare.providerservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Date;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProviderApiTest {
    @Autowired MockMvc mvc;
    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void searchAndAvailability() throws Exception {
        String admin = token("admin", "ADMIN");
        String body = "{\"name\":\"Dr Smith\",\"specialty\":\"Cardiology\",\"facility\":\"Central\",\"availableSlots\":\"2026-09-20T09:00\"}";
        String resp = mvc.perform(post("/api/v1/providers").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\"id\":")[1].split("[,}]")[0].trim();
        String patient = token("pat", "PATIENT");
        mvc.perform(get("/api/v1/providers/search?specialty=cardio").header("Authorization", "Bearer " + patient))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/providers/" + id + "/availability").header("Authorization", "Bearer " + patient))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/providers/search")).andExpect(status().isUnauthorized());
    }
}
