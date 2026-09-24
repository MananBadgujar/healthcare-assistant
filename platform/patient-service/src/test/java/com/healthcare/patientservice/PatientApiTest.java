package com.healthcare.patientservice;

import com.healthcare.patientservice.security.JwtUtil;
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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientApiTest {
    @Autowired MockMvc mvc;
    @Value("${jwt.secret}") String secret;

    String token(String user, String... roles) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().setSubject(user).claim("roles", List.of(roles))
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Test
    public void crudAndOwnership() throws Exception {
        String admin = token("admin1", "ADMIN");
        String body = "{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"dateOfBirth\":\"1990-01-01\",\"gender\":\"F\"}";
        String resp = mvc.perform(post("/api/v1/patients").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = resp.split("\"id\":")[1].split("[,}]")[0].trim();
        mvc.perform(get("/api/v1/patients/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/patients/" + id)).andExpect(status().isUnauthorized());
        String other = token("stranger", "PATIENT");
        mvc.perform(get("/api/v1/patients/" + id).header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/patients/999999").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }
}
