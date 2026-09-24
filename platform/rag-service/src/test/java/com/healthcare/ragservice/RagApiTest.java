package com.healthcare.ragservice;

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
public class RagApiTest {
    @Autowired MockMvc mvc;
    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void ingestSearchCite() throws Exception {
        String staff = token("s", "STAFF");
        mvc.perform(post("/api/v1/rag/ingest").header("Authorization", "Bearer " + staff)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Hypertension Guide\",\"content\":\"Hypertension management includes low sodium diet.\\n\\nBeta blockers reduce blood pressure.\"}"))
                .andExpect(status().isCreated());
        String patient = token("p", "PATIENT");
        mvc.perform(get("/api/v1/rag/search?q=blood+pressure").header("Authorization", "Bearer " + patient))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].documentId").exists());
        mvc.perform(post("/api/v1/rag/ask").header("Authorization", "Bearer " + patient)
                .contentType(MediaType.APPLICATION_JSON).content("{\"q\":\"blood pressure\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.citations").exists());
    }
}
