package com.healthcare.cdsservice;

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
public class CdsApiTest {
    @Autowired MockMvc mvc;
    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void detectsMajorInteraction() throws Exception {
        String provider = token("dr", "PROVIDER");
        mvc.perform(post("/api/v1/cds/check-interactions").header("Authorization", "Bearer " + provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"drugs\":[\"warfarin\",\"aspirin\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risk").value("HIGH"))
                .andExpect(jsonPath("$.requiresProviderReview").value(true));
        String patient = token("p", "PATIENT");
        mvc.perform(post("/api/v1/cds/check-interactions").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"drugs\":[\"warfarin\",\"aspirin\"]}"))
                .andExpect(status().isForbidden());
    }
}
