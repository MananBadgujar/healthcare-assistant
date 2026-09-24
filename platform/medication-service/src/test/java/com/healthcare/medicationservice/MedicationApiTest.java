package com.healthcare.medicationservice;

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
public class MedicationApiTest {
    @Autowired MockMvc mvc;
    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void formularyGuardrailAndRefill() throws Exception {
        String provider = token("dr", "PROVIDER");
        String resp = mvc.perform(post("/api/v1/medications").header("Authorization", "Bearer " + provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"name\":\"Paracetamol\",\"dosage\":\"500mg\",\"frequency\":\"BID\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\"id\":")[1].split("[,}]")[0].trim();
        mvc.perform(post("/api/v1/medications").header("Authorization", "Bearer " + provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"patientId\":1,\"name\":\"Unobtanium-X\",\"dosage\":\"1g\"}"))
                .andExpect(status().isBadRequest());
        String patient = token("p", "PATIENT");
        mvc.perform(post("/api/v1/medications/" + id + "/refill").header("Authorization", "Bearer " + patient))
                .andExpect(status().isOk());
    }
}
